package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.TestApplication;
import net.glenmazza.sfclient.model.AccountRecord;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ContextConfiguration(classes = TestApplication.class, initializers = ConfigDataApplicationContextInitializer.class)
public class SOQLQueryRunnerTest {

    @Autowired
    private SOQLQueryRunner sqr;

    @Test
    void testObjectQueryCalls() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SOQLQueryResponse<AccountRecord> result = sqr.runObjectQuery(
                "SELECT name,type FROM Account LIMIT 2", AccountRecord.class);

        // Test checks here are generic as actual results will depend on Accounts
        // in SF instance being queried.
        assertEquals(2, result.getTotalSize());
        assertNotNull(result.getRecords().get(0).getName());
        assertTrue(result.isDone());
    }

}
