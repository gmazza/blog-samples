package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.TestApplication;
import net.glenmazza.sfclient.model.AccountCreateRecord;
import net.glenmazza.sfclient.model.AccountQueryRecord;
import net.glenmazza.sfclient.model.AccountUpdateRecord;
import net.glenmazza.sfclient.model.RecordCreateResponse;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import net.glenmazza.sfclient.util.JSONUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
public class RESTServicesTest {

    @Autowired
    private SOQLQueryRunner sqr;

    @Autowired
    private SalesforceRecordManager srm;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void initialize() {
        objectMapper = JSONUtils.createObjectMapper();
    }

    // Exception handler
    // GET to query results and check data.

    @Test
    void testSRMInsertsGetsAndSOQLQueries() throws Exception {
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
    }

    @Test
    void testSRMUpdates() throws Exception {
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

}
