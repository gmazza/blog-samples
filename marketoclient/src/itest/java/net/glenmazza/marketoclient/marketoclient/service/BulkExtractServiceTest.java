package net.glenmazza.marketoclient.marketoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.marketoclient.marketoclient.TestApplication;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.CreateJobRequest;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.JobStatusResponse;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.ExtractFormat;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.ExtractType;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.Job;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("testing requires config setup in application-test.properties")
public class BulkExtractServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(BulkExtractServiceTest.class);

    @Autowired
    private BulkExtractService bulkExtractService;

    @Test
    void testLeadExtracts() throws JsonProcessingException {
        // create extract
        CreateJobRequest cer = new CreateJobRequest(ExtractType.LEADS, ExtractFormat.CSV);
        cer.setFields(List.of("email", "firstName", "lastName", "id", "cookies"));
        cer.setCreatedAtFilter(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        JobStatusResponse resp = bulkExtractService.createJob(cer);
        assertEquals(1, resp.getResult().size());
        assertTrue(resp.isSuccess());
        assertNull(resp.getErrors());
        Job job = resp.getResult().get(0);
        assertNotNull(job.getExportId());
        assertEquals(Job.Status.CREATED, job.getStatus());
        assertTrue(job.getCreatedAt().isBefore(OffsetDateTime.now().plusMinutes(10)));
        assertTrue(job.getCreatedAt().isAfter(OffsetDateTime.now().minusMinutes(10)));
        assertEquals(ExtractFormat.CSV, job.getFormat());

        // query by Job Status (here, Created), confirm exportId above in list
        List<Job.Status> jobStatuses = List.of(Job.Status.CREATED);
        JobStatusResponse jsr = bulkExtractService.getJobsByStatus(ExtractType.LEADS, jobStatuses);
        assertTrue(jsr.isSuccess());
        assertTrue(jsr.getResult().size() > 0);
        assertEquals(1, jsr.getResult().stream()
                .map(Job::getExportId).filter(eId -> eId.equals(job.getExportId())).count());

        // query (poll) by exportId, confirm Queued (or Processing or Completed)
        // running status subject to limits:
        // https://developers.marketo.com/rest-api/bulk-extract/#limits
        JobStatusResponse jsr2 = bulkExtractService.getJobStatus(ExtractType.LEADS, job.getExportId());
        assertTrue(jsr2.isSuccess());
        assertEquals(1, jsr2.getResult().size());
        Job job3 = jsr2.getResult().get(0);
        assertEquals(job.getExportId(), job3.getExportId());
        assertEquals(Job.Status.CREATED, job3.getStatus());
    }

    @Test
    void testActivityExtracts() throws IOException, InterruptedException {

        // create activity extract
        // https://developers.marketo.com/rest-api/bulk-extract/bulk-activity-extract/#creating_a_job
        CreateJobRequest cer = new CreateJobRequest(ExtractType.ACTIVITIES, ExtractFormat.CSV);
        cer.setCreatedAtFilter(LocalDateTime.now().minusDays(1), LocalDateTime.now());
        cer.setActivityIdsFilter(new Integer[]{1, 12, 13});
        JobStatusResponse resp = bulkExtractService.createJob(cer);
        assertTrue(resp.isSuccess());
        assertNull(resp.getErrors());
        assertEquals(1, resp.getResult().size());
        Job job = resp.getResult().get(0);
        assertNotNull(job.getExportId());
        assertEquals(Job.Status.CREATED, job.getStatus());

        // enqueue job
        JobStatusResponse jsr2 = bulkExtractService.enqueueJob(ExtractType.ACTIVITIES, job.getExportId());
        assertTrue(jsr2.isSuccess());
        assertEquals(1, jsr2.getResult().size());
        Job job2 = jsr2.getResult().get(0);
        assertEquals(job.getExportId(), job2.getExportId());
        assertTrue(List.of(Job.Status.QUEUED, Job.Status.PROCESSING, Job.Status.COMPLETED)
                .contains(job2.getStatus()), "Unexpected Status " + job2.getStatus());

        // query (poll) by exportId, confirm Queued (or Processing or Completed)
        // running status subject to limits:
        // https://developers.marketo.com/rest-api/bulk-extract/#limits
        JobStatusResponse jsr3 = bulkExtractService.getJobStatus(ExtractType.ACTIVITIES, job.getExportId());
        assertTrue(jsr3.isSuccess());
        assertEquals(1, jsr3.getResult().size());
        Job job3 = jsr3.getResult().get(0);
        assertEquals(job.getExportId(), job3.getExportId());
        assertTrue(List.of(Job.Status.QUEUED, Job.Status.PROCESSING, Job.Status.COMPLETED)
                .contains(job3.getStatus()), "Unexpected Status " + job3.getStatus());

        while (Job.Status.COMPLETED != job3.getStatus()) {
            LOGGER.info("Waiting 70sec for COMPLETED status for job with exportId {}, currently {}", job3.getExportId(), job3.getStatus());
            Thread.sleep(70000);
            jsr3 = bulkExtractService.getJobStatus(ExtractType.ACTIVITIES, job3.getExportId());
            job3 = jsr3.getResult().get(0);
        }

        LOGGER.info("Status of Job with export ID {} now {}, so reading data...", job3.getExportId(), job3.getStatus());
        String text = bulkExtractService.getFileData(ExtractType.ACTIVITIES, job3.getExportId());
        assertTrue(text.startsWith("marketoGUID,leadId,activityDate,activityTypeId,campaignId,primaryAttributeValueId"));
        LOGGER.info("Size of text: {}", text.length());
    }
}
