package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to run SOQL Queries:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api/dome_query.htm
 */
@Service
public class SOQLQueryRunner extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOQLQueryRunner.class);

    @Value("${salesforce.api.version:v50.0}")
    private String apiVersion;

    private final Map<Class<? extends SOQLQueryResponse.Record>, JavaType> parametricTypes = new HashMap<>();

    @Autowired
    public SOQLQueryRunner(WebClient webClient) {
        super(webClient);
    }

    // use to have Queries returned as Java objects
    public <T extends SOQLQueryResponse.Record> SOQLQueryResponse<T> runObjectQuery(String query,
            Class<? extends SOQLQueryResponse.Record> recordType) throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(recordType, this::createParametricJavaType);
        String jsonResult = runQuery(query);
        return objectMapper.readValue(jsonResult, type);
    }

    // use if a JSON of the results is sufficient.
    public String runQuery(String query) {
        return webClient
                .get()
                .uri(baseUrl + "/services/data/" + apiVersion + "/query?q=" + query)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    private JavaType createParametricJavaType(Class<? extends SOQLQueryResponse.Record> recordType) {
        JavaType jt = objectMapper.getTypeFactory().constructParametricType(SOQLQueryResponse.class, recordType);
        LOGGER.info("Created new JavaType for recordType {}", recordType.getName());
        return jt;
    }
}
