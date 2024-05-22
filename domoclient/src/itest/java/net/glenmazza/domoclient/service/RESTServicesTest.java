package net.glenmazza.domoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.domoclient.TestApplication;
import net.glenmazza.domoclient.model.DataSetColumn;
import net.glenmazza.domoclient.model.DataSetListRequest;
import net.glenmazza.domoclient.model.DataSetMetadata;
import net.glenmazza.domoclient.model.DataSetQueryRequest;
import net.glenmazza.domoclient.model.DataSetQueryResponse;
import net.glenmazza.domoclient.model.DataSetSchema;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * To run tests locally: will need to have Domo instance configured in an application-test.properties file
 * kept in project root, which will be merged with values in the same-named file in the resources file.
 * Then remove @Disabled annotation below.
 */
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("testing requires config setup in application-test.properties")
public class RESTServicesTest {

    @Autowired
    private DataSetQueryRunner queryRunner;

    @Test
    void testDataSetContentRetrieval() throws JsonProcessingException {
        // Will need a data set ID for your Domo instance (see blog entry given in README)
        // below is just a random UUID from a UUID generator
        String dataSetId = "79ec191f-1a35-4d2e-8133-9d34b2a9b065";

        // Will need to update col1->col3 with actual column names from the DataSet in your Domo instance
        // note the table name is literally "table" as given, dataSetId is what identifies the DataSet
        DataSetQueryRequest request = new DataSetQueryRequest(dataSetId,
                "Select col1, col2, col3 from table");

        DataSetQueryResponse response = queryRunner.runDataSetQuery(request);
        assertEquals(response.getNumRows(), response.getRows().size());
        assertEquals(dataSetId, response.getDatasource());
        assertEquals(3, response.getNumColumns());
        assertEquals("col2", response.getColumns().get(1));
        assertEquals(dataSetId, response.getMetadata().get(2).getDataSourceId());
    }

    @Test
    void testDataSetMetadataRetrieval() throws JsonProcessingException {
        // Update line below with some part of an already existing dataset name.
        DataSetListRequest dslr = new DataSetListRequest("portion of dataset name");
        dslr.setSortBy(DataSetListRequest.SortField.CREATED_AT_DESC);
        dslr.setOffset(0);
        dslr.setLimit(10);
        List<DataSetMetadata> dsmList = queryRunner.getDataSetMetadata(dslr);
        // below need updating based on dataset queried, can run test in IDE debugger to find out responses
        assertEquals(1, dsmList.size());
        DataSetMetadata dsm = dsmList.get(0);
        assertEquals(4, dsm.getColumns());
        assertNotNull(dsm.getId());
        assertTrue(dsm.getRows() > 10);
        assertEquals("actual dataset name", dsm.getName());
        assertEquals("developer name", dsm.getOwner().getName());
        assertFalse(dsm.isPdpEnabled());
        assertTrue(dsm.getCreatedAt().isBefore(dsm.getUpdatedAt()));
        assertTrue(dsm.getDataCurrentAt().isAfter(LocalDateTime.now().minus(5, ChronoUnit.DAYS)));
    }

    @Test
    void dataSetMetadataById() throws JsonProcessingException {
        // as with testDataSetContentRetrieval() above, will need actual dataSetId and to populate the
        // tests with expected value for it.
        String dataSetId = "79ec191f-1a35-4d2e-8133-9d34b2a9b065";
        DataSetMetadata response = queryRunner.getDataSetMetadataById(dataSetId);
        assertNotNull(response);
        assertEquals("Sample dataset name", response.getName());
        assertNotNull(response.getId());
        assertNotNull(response.getOwner());
        DataSetSchema schema = response.getSchema();
        assertNotNull(schema);
        List<DataSetColumn> columns = schema.getColumns();
        assertNotNull(columns);
        assertEquals(3, columns.size());
        DataSetColumn column0 = columns.get(0);
        assertEquals("column_name", column0.getName());
        assertEquals("STRING", column0.getType());
    }

}
