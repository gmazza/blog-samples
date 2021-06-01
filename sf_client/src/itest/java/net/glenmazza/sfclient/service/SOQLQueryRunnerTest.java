package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.TestApplication;
import net.glenmazza.sfclient.model.AccountCreateRecord;
import net.glenmazza.sfclient.model.AccountQueryRecord;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
@ActiveProfiles("test")
// Define "disabled if" test
public class SOQLQueryRunnerTest {

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
    void testSalesforceRecordManager() throws Exception {
        // insert first Account via Class & SOQL query
        AccountCreateRecord acr = new AccountCreateRecord();
        acr.setName("TestAccount");
        acr.setSite("Philadelphia");
        acr.setNumberOfEmployees(25);
        acr.setRating(AccountCreateRecord.RatingEnum.Hot);
        LocalDate expirationDate = LocalDate.now().plus(30, ChronoUnit.DAYS);
        acr.setSLAExpirationDate(expirationDate);
        RecordCreateResponse rcr1 = srm.createObject("Account", acr);
        assertTrue(rcr1.isSuccess());

        AccountCreateRecord resp = srm.getObject("Account", rcr1.getId(), AccountCreateRecord.class);
        assertEquals(acr.getSite(), resp.getSite());
        assertEquals(acr.getName(), resp.getName());
        assertEquals(acr.getNumberOfEmployees(), resp.getNumberOfEmployees());
        assertEquals(acr.getRating(), resp.getRating());
        assertEquals(acr.getSLAExpirationDate(), resp.getSLAExpirationDate());

        Map<String, Object> accountViaMap = new HashMap<>();
        accountViaMap.put("Name", "Test Account 2");
        accountViaMap.put("NumberOfEmployees", 30);
        accountViaMap.put("Site", "Baltimore");
        accountViaMap.put("Rating", "Cold");
        accountViaMap.put("SLAExpirationDate__c", expirationDate);
        RecordCreateResponse rcr2 = srm.createObject("Account", accountViaMap);
        assertTrue(rcr2.isSuccess());

        String respJson = srm.getJson("Account", rcr2.getId());
        var resp2 = objectMapper.readValue(respJson, AccountCreateRecord.class);
        assertEquals(accountViaMap.get("Name"), resp2.getName());
        assertEquals(accountViaMap.get("NumberOfEmployees"), resp2.getNumberOfEmployees());
        assertEquals(accountViaMap.get("Site"), resp2.getSite());
        assertEquals(accountViaMap.get("Rating"), resp2.getRating().name());
        assertEquals(accountViaMap.get("SLAExpirationDate__c"), resp2.getSLAExpirationDate());

        srm.deleteObject("Account", rcr1.getId());
        srm.deleteObject("Account", rcr2.getId());
    }


    @Test
    void testObjectQueryCalls() throws Exception {
        SOQLQueryResponse<AccountQueryRecord> result = sqr.runObjectQuery(
                "SELECT name,type FROM Account LIMIT 2", AccountQueryRecord.class);

        // Test checks here are generic as actual results will depend on Accounts
        // in SF instance being queried.
        assertEquals(2, result.getTotalSize());
        assertNotNull(result.getRecords().get(0).getName());
        assertTrue(result.isDone());
    }

}
