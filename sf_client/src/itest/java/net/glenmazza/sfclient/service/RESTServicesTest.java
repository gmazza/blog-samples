package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.TestApplication;
import net.glenmazza.sfclient.model.AccountCreateRecord;
import net.glenmazza.sfclient.model.AccountQueryRecord;
import net.glenmazza.sfclient.model.AccountUpdateRecord;
import net.glenmazza.sfclient.model.ApexAccountRecord;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases cover the three AbstractRESTService subclasses supporting Salesforce entity calls, Apex endpoints,
 * and SOQLQueries.
 *
 * Will need to have your test Salesforce instance configured in application-test.properties.  Also,
 * the Apex REST test requires a certain Apex endpoint to be installed see the header for that
 * test for more info.
 */
@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
@Disabled("Must first configure application-test.properties")
public class RESTServicesTest {

    @Autowired
    private SOQLQueryRunner sqr;

    @Autowired
    private SalesforceRecordManager srm;

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
    void testSRMInsertsGetsAndSOQLQueries() throws JsonProcessingException {
        // insert first Account via Class & SOQL query
        AccountCreateRecord acr = new AccountCreateRecord();
        acr.setName("Test Account");
        acr.setSite("Philadelphia");
        acr.setNumberOfEmployees(25);
        acr.setRating(AccountCreateRecord.RatingEnum.Hot);
        LocalDate expirationDate = LocalDate.now().plus(30, ChronoUnit.DAYS);
        acr.setSLAExpirationDate(expirationDate);
        RecordCreateResponse rcr1 = srm.create("Account", acr);
        assertTrue(rcr1.isSuccess());

        // query and check values
        AccountCreateRecord resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getName(), resp.getName());
        assertEquals(acr.getSite(), resp.getSite());
        assertEquals(acr.getNumberOfEmployees(), resp.getNumberOfEmployees());
        assertEquals(acr.getRating(), resp.getRating());
        assertEquals(acr.getSLAExpirationDate(), resp.getSLAExpirationDate());

        // create via Map
        Map<String, Object> accountViaMap = new HashMap<>();
        accountViaMap.put("Name", "Test Account 2");
        accountViaMap.put("Site", "Baltimore");
        accountViaMap.put("NumberOfEmployees", 30);
        accountViaMap.put("Rating", "Cold");
        accountViaMap.put("SLAExpirationDate__c", expirationDate);
        RecordCreateResponse rcr2 = srm.create("Account", accountViaMap);
        assertTrue(rcr2.isSuccess());

        // query via String
        String respJson = srm.getJson("Account", rcr2.getId());
        var resp2 = objectMapper.readValue(respJson, AccountCreateRecord.class);
        assertEquals(accountViaMap.get("Name"), resp2.getName());
        assertEquals(accountViaMap.get("Site"), resp2.getSite());
        assertEquals(accountViaMap.get("NumberOfEmployees"), resp2.getNumberOfEmployees());
        assertEquals(accountViaMap.get("Rating"), resp2.getRating().name());
        assertEquals(accountViaMap.get("SLAExpirationDate__c"), resp2.getSLAExpirationDate());

        // while we have two known accounts, check SOQL queries
        SOQLQueryResponse<AccountQueryRecord> result = sqr.runObjectQuery(
                String.format("SELECT name, site FROM Account WHERE Id in ('%s', '%s')",
                rcr1.getId(), rcr2.getId()),
                AccountQueryRecord.class);

        assertEquals(2, result.getTotalSize());
        assertTrue(result.isDone());
        assertTrue(result.getRecords().stream().anyMatch(a -> "Test Account".equals(a.getName())));
        assertTrue(result.getRecords().stream().filter(a -> "Test Account 2".equals(a.getName()))
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
    void testSRMUpdates() throws JsonProcessingException {
        // insert first Account via Class & SOQL query
        AccountCreateRecord acr = new AccountCreateRecord();
        acr.setName("Test Account 3");
        acr.setSite("Richmond");
        acr.setNumberOfEmployees(15);
        acr.setRating(AccountCreateRecord.RatingEnum.Cold);
        LocalDate expirationDate = LocalDate.now().plus(30, ChronoUnit.DAYS);
        acr.setSLAExpirationDate(expirationDate);
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
        assertEquals(acr.getSLAExpirationDate(), resp.getSLAExpirationDate());

        // update via Map
        Map<String, Object> changedVals = new HashMap<>();
        changedVals.put("Rating", "Warm");
        LocalDate newExpirationDate = expirationDate.plus(30, ChronoUnit.DAYS);
        changedVals.put("SLAExpirationDate__c", newExpirationDate);
        srm.update("Account", rcr1.getId(), changedVals);

        resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getName(), resp.getName());
        assertEquals("Norfolk", resp.getSite());
        assertEquals(25, resp.getNumberOfEmployees());
        assertEquals("Warm", resp.getRating().name());
        assertEquals(newExpirationDate, resp.getSLAExpirationDate());

        // cleanup
        srm.deleteObject("Account", rcr1.getId());
    }

    @Test
    // Must have MyRestResource installed
    // is here: https://developer.salesforce.com/docs/atlas.en-us.224.0.apexcode.meta/apexcode/apex_rest_code_sample_basic.htm
    void testApexRestCalls() throws JsonProcessingException {
        // post company
        Map<String, Object> createAccountMap = new HashMap<>();
        createAccountMap.put("name", "Wingo Ducks");
        createAccountMap.put("phone", "707-555-1234");
        createAccountMap.put("website", "www.wingo.ca");
        String id = arc.makeCall("Account", HttpMethod.POST, createAccountMap);
        // REST endpoint returns id in quotes
        id = id.replace("\"", "");

        // query company into Map
        String company = arc.get("Account/" + id);
        Map<String, Object> companyVals = objectMapper.readValue(company, new TypeReference<>() { });
        assertEquals(companyVals.get("Id"), id);
        assertEquals(companyVals.get("Name"), "Wingo Ducks");
        assertEquals(companyVals.get("Phone"), "707-555-1234");
        assertEquals(companyVals.get("Website"), "www.wingo.ca");

        // query company into Object
        ApexAccountRecord aar = arc.getObject("Account/" + id, ApexAccountRecord.class);
        assertEquals(aar.getId(), id);
        assertEquals(aar.getName(), "Wingo Ducks");
        assertEquals(aar.getPhone(), "707-555-1234");
        assertEquals(aar.getWebsite(), "www.wingo.ca");

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
