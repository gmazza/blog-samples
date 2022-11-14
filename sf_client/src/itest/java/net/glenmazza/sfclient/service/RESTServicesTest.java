package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.TestApplication;
import net.glenmazza.sfclient.model.AccountCreateRecord;
import net.glenmazza.sfclient.model.AccountMultipleEntityRecord;
import net.glenmazza.sfclient.model.AccountQueryRecord;
import net.glenmazza.sfclient.model.AccountUpdateRecord;
import net.glenmazza.sfclient.model.ApexAccountRecord;
import net.glenmazza.sfclient.model.MultipleEntityRecord;
import net.glenmazza.sfclient.model.MultipleEntityRecord201Response;
import net.glenmazza.sfclient.model.MultipleEntityRecord400ResponseException;
import net.glenmazza.sfclient.model.MultipleEntityRecordRequest;
import net.glenmazza.sfclient.model.RecordCreateResponse;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import net.glenmazza.sfclient.model.ServiceException;
import net.glenmazza.sfclient.util.JSONUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test cases cover the three AbstractRESTService subclasses supporting Salesforce entity calls, Apex endpoints,
 * and SOQLQueries.
 *
 * Will need to have your test Salesforce instance configured in application-test.properties~template.  Also,
 * the Apex REST test requires a certain Apex endpoint to be installed see the header for that
 * test for more info.
 */
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("testing requires config setup in application.properties")
public class RESTServicesTest {

    @Autowired
    private SOQLQueryRunner sqr;

    @Autowired
    private SalesforceRecordManager srm;

    @Autowired
    private SalesforceMultipleRecordInserter smri;

    @Autowired
    private ApexRESTCaller arc;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void initialize() {
        objectMapper = JSONUtils.createObjectMapper();
    }

    // Exception handler
    // GET to query results and check data.

    @Test
    void testSalesforceRecordManagerInsertsGetsAndSOQLQueries() throws JsonProcessingException {
        // insert first Account via Class & SOQL query
        AccountCreateRecord acr = new AccountCreateRecord();
        String account1Name = "Test Account " + LocalDate.now();
        acr.setName(account1Name);
        acr.setSite("Philadelphia");
        acr.setNumberOfEmployees(25);
        acr.setRating(AccountCreateRecord.RatingEnum.Hot);
        LocalDate leadDate = LocalDate.now().plus(30, ChronoUnit.DAYS);
        acr.setLeadDate(leadDate);
        RecordCreateResponse rcr1 = srm.create("Account", acr);
        assertTrue(rcr1.isSuccess());

        // query and check values
        AccountCreateRecord resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getName(), resp.getName());
        assertEquals(acr.getSite(), resp.getSite());
        assertEquals(acr.getNumberOfEmployees(), resp.getNumberOfEmployees());
        assertEquals(acr.getRating(), resp.getRating());
        assertEquals(acr.getLeadDate(), resp.getLeadDate());

        // create via Map
        Map<String, Object> accountViaMap = new HashMap<>();
        String account2Name = "Test Account 2 " + LocalDate.now();
        accountViaMap.put("Name", account2Name);
        accountViaMap.put("Site", "Baltimore");
        accountViaMap.put("NumberOfEmployees", 30);
        accountViaMap.put("Rating", "Cold");
        accountViaMap.put("Lead_Date__c", leadDate);
        RecordCreateResponse rcr2 = srm.create("Account", accountViaMap);
        assertTrue(rcr2.isSuccess());

        // query via String
        String respJson = srm.getJson("Account", rcr2.getId());
        var resp2 = objectMapper.readValue(respJson, AccountCreateRecord.class);
        assertEquals(accountViaMap.get("Name"), resp2.getName());
        assertEquals(accountViaMap.get("Site"), resp2.getSite());
        assertEquals(accountViaMap.get("NumberOfEmployees"), resp2.getNumberOfEmployees());
        assertEquals(accountViaMap.get("Rating"), resp2.getRating().name());
        assertEquals(accountViaMap.get("Lead_Date__c"), resp2.getLeadDate());

        // while we have two known accounts, check SOQL queries
        SOQLQueryResponse<AccountQueryRecord> result = sqr.runObjectQuery(
                String.format("SELECT name, site FROM Account WHERE Id in ('%s', '%s')",
                        rcr1.getId(), rcr2.getId()),
                AccountQueryRecord.class);

        assertEquals(2, result.getTotalSize());
        assertTrue(result.isDone());
        assertTrue(result.getRecords().stream().anyMatch(a -> account1Name.equals(a.getName())));
        assertTrue(result.getRecords().stream().filter(a -> account2Name.equals(a.getName()))
                .anyMatch(a -> "Baltimore".equals(a.getSite())));

        // cleanup
        srm.deleteObject("Account", rcr1.getId());
        srm.deleteObject("Account", rcr2.getId());

        // test 404 will occur as object no longer present
        int errorCode = 0;
        try {
            srm.deleteObject("Account", rcr2.getId());
            Assertions.fail("Should have thrown 404");
        } catch (ServiceException e) {
            errorCode = e.getStatusCode();
        }
        assertEquals(404, errorCode);
    }

    @Test
    void testSalesforceRecordManagerUpdates() throws JsonProcessingException {
        // insert first Account via Class & SOQL query
        AccountCreateRecord acr = new AccountCreateRecord();
        acr.setName("Test Account 3");
        acr.setSite("Richmond");
        acr.setNumberOfEmployees(15);
        acr.setRating(AccountCreateRecord.RatingEnum.Cold);
        LocalDate leadDate = LocalDate.now().plus(30, ChronoUnit.DAYS);
        acr.setLeadDate(leadDate);
        RecordCreateResponse rcr1 = srm.create("Account", acr);
        assertTrue(rcr1.isSuccess());

        // update via Java class
        AccountUpdateRecord aur = new AccountUpdateRecord();
        aur.setSite("Norfolk");
        aur.setNumberOfEmployees(25);
        srm.update("Account", rcr1.getId(), aur);

        // query and check values
        AccountCreateRecord resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getName(), resp.getName());
        assertEquals("Norfolk", resp.getSite());
        assertEquals(25, resp.getNumberOfEmployees());
        assertEquals(acr.getRating(), resp.getRating());
        assertEquals(acr.getLeadDate(), resp.getLeadDate());

        // update via Map
        Map<String, Object> changedVals = new HashMap<>();
        changedVals.put("Rating", "Warm");
        LocalDate newLeadDate = leadDate.plus(30, ChronoUnit.DAYS);
        changedVals.put("Lead_Date__c", newLeadDate);
        srm.update("Account", rcr1.getId(), changedVals);

        resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getName(), resp.getName());
        assertEquals("Norfolk", resp.getSite());
        assertEquals(25, resp.getNumberOfEmployees());
        assertEquals("Warm", resp.getRating().name());
        assertEquals(newLeadDate, resp.getLeadDate());

        // cleanup
        srm.deleteObject("Account", rcr1.getId());
    }

    @Test
    void testMultipleEntityRecordInsertions() throws JsonProcessingException {
        AccountMultipleEntityRecord amer = new AccountMultipleEntityRecord("111");
        amer.setName("Test MER Account1");
        amer.setSite("Raleigh");
        amer.setNumberOfEmployees(20);
        amer.setRating(AccountMultipleEntityRecord.RatingEnum.Cold);
        LocalDate leadDate = LocalDate.now().plus(20, ChronoUnit.DAYS);
        amer.setLeadDate(leadDate);

        AccountMultipleEntityRecord amer2 = new AccountMultipleEntityRecord("222");
        amer2.setName("Test MER Account2");
        amer2.setSite("Charlotte");
        amer2.setNumberOfEmployees(30);
        amer2.setRating(AccountMultipleEntityRecord.RatingEnum.Hot);
        LocalDate leadDate2 = LocalDate.now().plus(10, ChronoUnit.DAYS);
        amer2.setLeadDate(leadDate2);

        MultipleEntityRecordRequest<AccountMultipleEntityRecord> merr = new MultipleEntityRecordRequest<>();
        merr.setRecords(List.of(amer, amer2));

        MultipleEntityRecord201Response response = smri.bulkInsert("Account", merr);
        assertEquals(2, response.getResults().size());

        // method also deletes objects once done comparing
        queryAndConfirmValues(response, amer);
        queryAndConfirmValues(response, amer2);

        // now test exception handling:  will activate by having both accounts have the same reference ID.
        amer2.setAttributes(new MultipleEntityRecord.Attributes("Account", "111"));

        /* Error response should be:
         * {
         *     "hasErrors": true,
         *     "results": [
         *         {
         *             "referenceId": "111",
         *             "errors": [
         *                 {
         *                     "statusCode": "INVALID_INPUT",
         *                     "message": "Duplicate ReferenceId provided in the request.",
         *                     "fields": []
         *                 }
         *             ]
         *         },
         *     ]
         * }
         */

        boolean hasErrors = false;
        int errorCount = 0;
        String referenceId = null;
        String statusCode = null;
        String message = null;

        try {
            smri.bulkInsert("Account", merr);
        } catch (MultipleEntityRecord400ResponseException ex) {
            hasErrors = ex.getResponse().isHasErrors();
            List<MultipleEntityRecord400ResponseException.Response.Result> errorResults = ex.getResponse().getResults();
            errorCount = errorResults.size();
            referenceId = errorResults.get(0).getReferenceId();
            statusCode = errorResults.get(0).getErrors().get(0).getStatusCode();
            message = errorResults.get(0).getErrors().get(0).getMessage();
        }

        assertTrue(hasErrors);
        assertEquals(1, errorCount);
        assertEquals("111", referenceId);
        assertEquals("INVALID_INPUT", statusCode);
        assertEquals("Duplicate ReferenceId provided in the request.", message);
    }

    private void queryAndConfirmValues(MultipleEntityRecord201Response response, AccountMultipleEntityRecord amer)
            throws JsonProcessingException {
        String accountSfId = response.getResults().stream()
                .filter(r -> amer.getAttributes().getReferenceId().equals(r.getReferenceId()))
                .map(MultipleEntityRecord201Response.Result::getId).findFirst().orElseThrow();

        // query and check values
        AccountCreateRecord acr = srm.getObject("Account", accountSfId, AccountCreateRecord.class);
        assertEquals(acr.getName(), amer.getName());
        assertEquals(acr.getSite(), amer.getSite());
        assertEquals(acr.getNumberOfEmployees(), amer.getNumberOfEmployees());
        assertEquals(acr.getRating().name(), amer.getRating().name());
        assertEquals(acr.getLeadDate(), amer.getLeadDate());

        srm.deleteObject("Account", accountSfId);
    }

    @Test
        // Must have MyRestResource installed
        // it is here: https://developer.salesforce.com/docs/atlas.en-us.224.0.apexcode.meta/apexcode/apex_rest_code_sample_basic.htm
        // Then go to setup, search on MyRestResource and ensure salesforce.oauth2.resourceowner.username user's profile has
        // access to this method (Security button to configure).
    void testApexRestCalls() throws JsonProcessingException {
        // post company
        Map<String, Object> createAccountMap = new HashMap<>();
        createAccountMap.put("name", "Wingo Ducks");
        createAccountMap.put("phone", "707-555-1234");
        createAccountMap.put("website", "www2.wingo.ca");
        String id = arc.makeCall("Account", HttpMethod.POST, createAccountMap);
        // REST endpoint returns id in quotes
        id = id.replace("\"", "");

        // query company into Map
        String company = arc.get("Account/" + id);
        Map<String, Object> companyVals = objectMapper.readValue(company, new TypeReference<>() { });
        assertEquals(companyVals.get("Id"), id);
        assertEquals("Wingo Ducks", companyVals.get("Name"));
        assertEquals("707-555-1234", companyVals.get("Phone"));
        assertEquals("www2.wingo.ca", companyVals.get("Website"));

        // query company into Object
        ApexAccountRecord aar = arc.getObject("Account/" + id, ApexAccountRecord.class);
        assertEquals(id, aar.getId());
        assertEquals("Wingo Ducks", aar.getName());
        assertEquals("707-555-1234", aar.getPhone());
        assertEquals("www2.wingo.ca", aar.getWebsite());

        // delete company
        arc.makeCall("Account/" + id, HttpMethod.DELETE);

        // test 500 will occur if object no longer present
        // (Apex REST API has an internal error in that case).
        int errorCode = 0;
        try {
            arc.makeCall("Account/" + id, HttpMethod.DELETE);
            Assertions.fail("Should have thrown 404");
        } catch (ServiceException e) {
            errorCode = e.getStatusCode();
        }
        assertEquals(500, errorCode);
    }

}
