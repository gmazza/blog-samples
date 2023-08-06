package net.glenmazza.domoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.domoclient.TestApplication;
import net.glenmazza.domoclient.model.DataSetQueryRequest;
import net.glenmazza.domoclient.model.DataSetQueryResponse;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void testDataSetQueries() throws JsonProcessingException {
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

}
