package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class SOQLQueryRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOQLQueryRunner.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${salesforce.api.base-url}")
    private String baseUrl;

    @Value("${salesforce.connection-timeout-ms:60000}")
    private int connectionTimeoutMs;

    @Value("${salesforce.socket-timeout-ms:30000}")
    private int socketTimeoutMs;

    @Value("${salesforce.api.version:v50.0}")
    private String apiVersion;

    private Map<Class<? extends SOQLQueryResponse.Record>, JavaType> parametricTypes = new HashMap<>();

    public SOQLQueryRunner(WebClient webClient) {
        this.webClient = webClient;

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
                // allow "Name" in JSON to map to "name" in class
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                // timestamps to Instant (https://stackoverflow.com/q/45762857/1207540)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    // use to have Queries returned as Java objects
    public <T extends SOQLQueryResponse.Record> SOQLQueryResponse<T> runObjectQuery(String query,
            Class<? extends SOQLQueryResponse.Record> recordType) throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(recordType, this::recordToJavaType);
        String jsonResult = runQuery(query);
        return objectMapper.readValue(jsonResult, type);
    }

    // method useful if a JSON of the results is sufficient.
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

    private JavaType recordToJavaType(Class<? extends SOQLQueryResponse.Record> recordType) {
        JavaType jt = objectMapper.getTypeFactory().constructParametricType(SOQLQueryResponse.class, recordType);
        LOGGER.info("Created new JavaType for recordType {}", recordType.getName());
        return jt;
    }
}
