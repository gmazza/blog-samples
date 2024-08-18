package net.glenmazza.marketoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.marketoclient.TestApplication;
import net.glenmazza.marketoclient.model.GiftingLeadRecord;
import net.glenmazza.marketoclient.model.GiftingLeadWithIDRecord;
import net.glenmazza.marketoclient.model.leads.LeadDeleteRequest;
import net.glenmazza.marketoclient.model.ResponseError;
import net.glenmazza.marketoclient.model.leads.LeadQueryRequest;
import net.glenmazza.marketoclient.model.leads.LeadQueryResponse;
import net.glenmazza.marketoclient.model.leads.LeadUpsertRequest;
import net.glenmazza.marketoclient.model.leads.LeadUpsertResponse;
import net.glenmazza.marketoclient.model.leads.LeadUpsertResponse.Result.Status;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("testing requires config setup in application-test.properties")
public class LeadServiceTest {

    @Autowired
    private LeadService leadService;

    private static final String EMAIL1 = "test101@yopmail.com";
    private static final String EMAIL2 = "test102@yopmail.com";

    // Marketo picky with date types, supported formats OR 2023-09-13T14:33:16-04:00 2023-09-13T17:51:27.492+00:00 (no nanoseconds)
    // DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX") or ss.SSSXXX
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXXX");

    @Test
    void testLeadMaintenance() throws JsonProcessingException {
        // insert two leads
        LeadUpsertRequest<GiftingLeadRecord> lur = new LeadUpsertRequest<>();
        List<GiftingLeadRecord> input = new ArrayList<>();
        lur.setInput(input);
        input.add(createGLR(EMAIL1, 5, -10));
        input.add(createGLR(EMAIL2, 10, +10));
        LeadUpsertResponse luResp = leadService.runLeadUpsertRequest(lur);
        validateLeadUpdateResponse(luResp, Status.CREATED);

        int email1Id = luResp.getResult().get(0).getId();
        int email2Id = luResp.getResult().get(1).getId();

        // query two leads by email
        LeadQueryRequest lqrByEmail = createLeadQueryRequest();
        LeadQueryResponse<GiftingLeadWithIDRecord> lqResp = leadService.runLeadQueryRequest(lqrByEmail, GiftingLeadWithIDRecord.class);
        validateLeadQueryResponse(lqResp, email1Id, email2Id, 5, 10, true);

        // update two leads
        input.clear();
        input.add(createGLR(EMAIL1, 15, +20));
        input.add(createGLR(EMAIL2, 20, -20));
        luResp = leadService.runLeadUpsertRequest(lur);
        validateLeadUpdateResponse(luResp, Status.UPDATED);
        assertTrue(luResp.getResult().stream().anyMatch(r -> r.getId() == email1Id));
        assertTrue(luResp.getResult().stream().anyMatch(r -> r.getId() == email2Id));

        // query two leads to confirm updated, this time by ID
        LeadQueryRequest lqrById = new LeadQueryRequest();
        lqrById.setFilterType(LeadQueryRequest.FilterType.ID);
        lqrById.setFields(lqrByEmail.getFields());
        lqrById.setFilterValues(List.of(String.valueOf(email1Id), String.valueOf(email2Id)));
        LeadQueryResponse<GiftingLeadWithIDRecord> lqResp2 = leadService.runLeadQueryRequest(lqrById, GiftingLeadWithIDRecord.class);
        validateLeadQueryResponse(lqResp2, email1Id, email2Id, 15, 20, false);

        // delete two leads
        deleteLeads(email1Id, email2Id);

        // query two leads to confirm deleted, both by email and ID
        lqResp2 = leadService.runLeadQueryRequest(lqrByEmail, GiftingLeadWithIDRecord.class);
        assertEquals(0, lqResp2.getResult().size());
        assertTrue(lqResp2.isSuccess());

        lqResp2 = leadService.runLeadQueryRequest(lqrById, GiftingLeadWithIDRecord.class);
        assertEquals(0, lqResp2.getResult().size());
        assertTrue(lqResp2.isSuccess());
    }

    @Test
    void testBadInsertIsSkippedWithErrorMessage() throws JsonProcessingException {

        LeadUpsertRequest<GiftingLeadRecord> lur = new LeadUpsertRequest<>();
        List<GiftingLeadRecord> input = new ArrayList<>();
        lur.setInput(input);
        // no email address will throw error
        input.add(createGLR(null, 5, -10));
        LeadUpsertResponse luResp = leadService.runLeadUpsertRequest(lur);

        assertNotNull(luResp.getRequestId());
        assertTrue(luResp.isSuccess());
        assertEquals(1, luResp.getResult().size());
        LeadUpsertResponse.Result result = luResp.getResult().get(0);
        assertEquals(Status.SKIPPED, result.getStatus());
        assertEquals(1, result.getReasons().size());
        ResponseError error = result.getReasons().get(0);
        assertEquals("1003", error.getCode());
        assertEquals("Value for required field 'email' not specified", error.getMessage());
    }


    @Test
    void testLeadMaintenanceViaMap() throws JsonProcessingException {
        // add a lead via a Map
        List<Map<String, Object>> inputItems = new ArrayList<>();
        inputItems.add(createLead(EMAIL1, 3, -20));
        inputItems.add(createLead(EMAIL2, 4, +20));

        LeadUpsertResponse luResp = leadService.upsertByEmail(inputItems);
        validateLeadUpdateResponse(luResp, Status.CREATED);

        int email1Id = luResp.getResult().get(0).getId();
        int email2Id = luResp.getResult().get(1).getId();

        // query values
        LeadQueryRequest lqrByEmail = createLeadQueryRequest();
        LeadQueryResponse<GiftingLeadWithIDRecord> lqResp =
                leadService.runLeadQueryRequest(lqrByEmail, GiftingLeadWithIDRecord.class);
        validateLeadQueryResponse(lqResp, email1Id, email2Id, 3, 4, true);

        deleteLeads(email1Id, email2Id);
    }

    @Test
    void testLeadMaintenanceViaMapExceptions() {
        List<Map<String, Object>> inputItems = new ArrayList<>();

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> leadService.upsertByEmail(inputItems));

        assertTrue(e.getMessage().contains("Input required"));

        inputItems.add(createLead("", 3, 20));
        inputItems.add(createLead(null, 3, 20));

        e = assertThrows(IllegalArgumentException.class,
                () -> leadService.upsertByEmail(inputItems));

        assertTrue(e.getMessage().contains("found missing on 2"));
    }

    @Test
    void testLeadMaintenanceViaString() throws JsonProcessingException {
        Map<String, Object> leadsToUpsert = new HashMap<>();
        leadsToUpsert.put("action", "createOrUpdate");
        leadsToUpsert.put("lookupField", "email");
        List<Map<String, Object>> inputItems = new ArrayList<>();
        leadsToUpsert.put("input", inputItems);
        inputItems.add(createLead(EMAIL1, 10, +20));
        inputItems.add(createLead(EMAIL2, 6, -10));

        String request = new ObjectMapper().writeValueAsString(leadsToUpsert);
        /*
        {"lookupField":"email","input":[
            {"peekabooSharingPROArticles":10,"peekabooSharingLastDatePROArticle":"2023-09-13T15:18:04.426-04:00","email":"test101@yopmail.com"},
            {"peekabooSharingPROArticles":6,"peekabooSharingLastDatePROArticle":"2023-09-13T14:48:08.780-04:00","email":"test102@yopmail.com"}
            ],
          "action":"createOrUpdate"}
        */
        LeadUpsertResponse luResp = leadService.runUpsertString(request);
        validateLeadUpdateResponse(luResp, Status.CREATED);

        int email1Id = luResp.getResult().get(0).getId();
        int email2Id = luResp.getResult().get(1).getId();

        // query values
        LeadQueryRequest lqrByEmail = createLeadQueryRequest();
        LeadQueryResponse<GiftingLeadWithIDRecord> lqResp =
                leadService.runLeadQueryRequest(lqrByEmail, GiftingLeadWithIDRecord.class);
        validateLeadQueryResponse(lqResp, email1Id, email2Id, 10, 6, false);

        deleteLeads(email1Id, email2Id);
    }

    private void validateLeadQueryResponse(LeadQueryResponse<GiftingLeadWithIDRecord> lqResp,
                                           int firstUserId, int secondUserId,
                                           int firstUserSends, int secondUserSends,
                                           boolean firstEarliestSharing) {

        assertEquals(2, lqResp.getResult().size());

        GiftingLeadWithIDRecord glr1 = lqResp.getResult().stream().filter(
                r -> EMAIL1.equals(r.getEmail())).findFirst().orElse(null);
        assertNotNull(glr1);
        assertEquals(firstUserId, glr1.getId());
        GiftingLeadWithIDRecord glr2 = lqResp.getResult().stream().filter(
                r -> EMAIL2.equals(r.getEmail())).findFirst().orElse(null);
        assertNotNull(glr2);
        assertEquals(secondUserId, glr2.getId());
        assertEquals(firstUserSends, glr1.getPeekabooSharingPROArticles());
        assertEquals(secondUserSends, glr2.getPeekabooSharingPROArticles());
        boolean firstFirst = glr1.getPeekabooSharingLastDatePROArticle().before(glr2.getPeekabooSharingLastDatePROArticle());
        assertEquals(firstEarliestSharing, firstFirst);
    }


    private LeadQueryRequest createLeadQueryRequest() {
        LeadQueryRequest lqrByEmail = new LeadQueryRequest();
        lqrByEmail.setFields(List.of("email", "peekabooSharingPROArticles", "peekabooSharingLastDatePROArticle"));
        lqrByEmail.setFilterType(LeadQueryRequest.FilterType.EMAIL);
        lqrByEmail.setFilterValues(List.of(EMAIL1, EMAIL2));
        return lqrByEmail;
    }

    private Map<String, Object> createLead(String email, int numArticles, int minOffset) {
        Map<String, Object> lead = new HashMap<>();
        lead.put("email", email);
        lead.put("peekabooSharingPROArticles", numArticles);
        lead.put("peekabooSharingLastDatePROArticle",  OffsetDateTime.now().plus(minOffset, ChronoUnit.MINUTES).format(DATE_TIME_FORMATTER));
        return lead;
    }

    private void validateLeadUpdateResponse(LeadUpsertResponse luResp, Status statusExpected) {
        assertNotNull(luResp.getRequestId());
        assertTrue(luResp.isSuccess());
        assertEquals(2, luResp.getResult().size());
        assertTrue(luResp.getResult().stream().allMatch(r -> statusExpected.equals(r.getStatus())));
        assertTrue(luResp.getResult().stream().allMatch(r -> r.getId() != 0));
    }

    private void deleteLeads(int email1Id, int email2Id) throws JsonProcessingException {
        LeadDeleteRequest ldr = new LeadDeleteRequest();
        List<LeadDeleteRequest.Input> deleteInput = new ArrayList<>();
        ldr.setInput(deleteInput);
        deleteInput.add(new LeadDeleteRequest.Input(email1Id));
        deleteInput.add(new LeadDeleteRequest.Input(email2Id));
        LeadUpsertResponse lur2 = leadService.runLeadDeleteRequest(ldr);
        assertNotNull(lur2.getRequestId());
        assertTrue(lur2.isSuccess());
        assertEquals(2, lur2.getResult().size());
        assertTrue(lur2.getResult().stream().allMatch(r -> Status.DELETED.equals(r.getStatus())));
        assertTrue(lur2.getResult().stream().anyMatch(r -> email1Id == r.getId()));
        assertTrue(lur2.getResult().stream().anyMatch(r -> email2Id == r.getId()));
    }

    private GiftingLeadRecord createGLR(String email, int count, int minsDelta) {
        GiftingLeadRecord glr = new GiftingLeadRecord();
        glr.setEmail(email);
        glr.setPeekabooSharingPROArticles(count);
        glr.setPeekabooSharingLastDatePROArticle(
                Date.from(Instant.now().plus(minsDelta, ChronoUnit.MINUTES)));
        return glr;
    }

}
