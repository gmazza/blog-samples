package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.sfclient.model.EntityRecord;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service to run SOQL Queries:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api/dome_query.htm
 */
@Service
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
public class SOQLQueryRunner extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOQLQueryRunner.class);

    @Value("${salesforce.api.version:v57.0}")
    private String apiVersion;

    private final Map<Class<? extends EntityRecord>, JavaType> parametricTypes = new HashMap<>();

    @Autowired
    public SOQLQueryRunner(WebClient webClient) {
        super(webClient);
    }

    // use to have Queries returned as Java objects
    public <T extends EntityRecord> SOQLQueryResponse<T> runObjectQuery(String query,
                                                                        Class<? extends EntityRecord> recordType)
            throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(recordType, this::createParametricJavaType);
        String jsonResult = runQuery(query);
        return objectMapper.readValue(jsonResult, type);
    }

    // use if a JSON of the results is sufficient.
    public String runQuery(String query) {

        // Emails with a plus in them (e.g., bob+smith@yopmail.com) need to have the + encoded for Salesforce
        // not to treat it as a space (the usual role of + signs).  Normal URI encoding doesn't encode it,
        // hence need to use buildAndExpand() below for that.
        // lengthy discussion: https://github.com/spring-projects/spring-framework/issues/21399
        // solution: https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#web-uri-encoding
        final URI uri =
                UriComponentsBuilder.fromHttpUrl(baseUrl + "/services/data/" + apiVersion + "/query")
                        .queryParam("q", "{q}")
                        .encode()
                        .buildAndExpand(query)
                        .toUri();

        return webClient
                .get()
                .uri(uri)
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

    private JavaType createParametricJavaType(Class<? extends EntityRecord> recordType) {
        JavaType jt = objectMapper.getTypeFactory().constructParametricType(SOQLQueryResponse.class, recordType);
        LOGGER.info("Created new JavaType for recordType {}", recordType.getName());
        return jt;
    }

    /**
     * Helper method when having emails in query strings.
     * In query strings, apostrophes in emails e.g.: o'brien@yopmail.com need to be replaced with \'
     */
    public static String safeEmail(String email) {
        return Optional.ofNullable(email).map(e -> e.replace("'", "\\'")).orElse(null);
    }
}
