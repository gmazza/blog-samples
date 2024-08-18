package net.glenmazza.marketoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;

import net.glenmazza.marketoclient.annotation.MarketoRetryable;
import net.glenmazza.marketoclient.model.leads.LeadDeleteRequest;
import net.glenmazza.marketoclient.model.leads.LeadQueryRequest;
import net.glenmazza.marketoclient.model.leads.LeadQueryResponse;
import net.glenmazza.marketoclient.model.leads.LeadUpsertRecord;
import net.glenmazza.marketoclient.model.leads.LeadUpsertRequest;
import net.glenmazza.marketoclient.model.leads.LeadUpsertResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Supports:
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#query">Get Leads by Filter Type</a>
 * and
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#create_and_update">Create and update leads</a>
 */
public class LeadService extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeadService.class);

    public LeadService(WebClient webClient, OAuth2AuthorizationFailureHandler failureHandler) {
        super(webClient, failureHandler);
        this.authorizationFailureHandler = failureHandler;
    }

    // needed for Jackson deserialization of parametric type used in LeadQueryResponse
    private final Map<Class<? extends LeadUpsertRecord>, JavaType> parametricTypes = new HashMap<>();

    // config has delays of 2, 22, 42, and 62 seconds
    // past one minute (after 42) should be sufficient for triggering a new access token from Marketo.
    @MarketoRetryable
    public <T extends LeadUpsertRecord> LeadQueryResponse<T> runLeadQueryRequest(LeadQueryRequest request,
                                                                                 Class<? extends LeadUpsertRecord> recordType)
            throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(recordType, this::createParametricJavaType);
        String jsonResult = runQuery(request);
        LeadQueryResponse<T> response = objectMapper.readValue(jsonResult, type);
        checkForMarketoErrorResponses(response);
        return response;
    }

    private String runQuery(LeadQueryRequest request) {

        if (request.getFilterType() == null) {
            throw new IllegalArgumentException("Filter type required");
        }

        if (ObjectUtils.isEmpty(request.getFilterValues())) {
            throw new IllegalArgumentException("Filter values (i.e., leads to return) required");
        }

        String queryString = String.format("filterType=%s&filterValues=%s",
                request.getFilterType().getFieldName(),
                String.join(",", request.getFilterValues()));

        // if not using default return fields
        if (!ObjectUtils.isEmpty(request.getFields())) {
            queryString += "&fields=" + String.join(",", request.getFields());
        }

        return webClient
                .get()
                .uri(baseUrl + String.format("/rest/v1/leads.json?%s", queryString))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                //.contentType(MediaType.APPLICATION_JSON)
                //.bodyValue(queryString)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();
    }

    @MarketoRetryable
    public LeadUpsertResponse runLeadUpsertRequest(LeadUpsertRequest<?> request) throws JsonProcessingException {
        return runUpsert(request);
    }

    private LeadUpsertResponse runUpsert(LeadUpsertRequest<?> request) throws JsonProcessingException {
        if (request.getAction() == null) {
            throw new IllegalArgumentException("Action required");
        }

        if (request.getLookupField() == null) {
            throw new IllegalArgumentException("Lookup field required");
        }

        if (ObjectUtils.isEmpty(request.getInput())) {
            throw new IllegalArgumentException("Input required");
        }

        String jsonString = objectMapper.writeValueAsString(request);
        return runMarketoRequest(this::runUpsertStringInternal, jsonString);
    }

    @MarketoRetryable
    public LeadUpsertResponse upsertByEmail(List<Map<String, Object>> leads) throws JsonProcessingException {
        if (ObjectUtils.isEmpty(leads)) {
            throw new IllegalArgumentException("Input required");
        }

        long noEmailCount = leads.stream().filter(l -> StringUtils.isBlank((String) l.get("email"))).count();
        if (noEmailCount > 0) {
            throw new IllegalArgumentException(
                String.format("Field 'email' needed for all leads, found missing on %d", noEmailCount));
        }

        Map<String, Object> leadsToUpsert = new HashMap<>();
        leadsToUpsert.put("action", "createOrUpdate");
        leadsToUpsert.put("lookupField", "email");
        leadsToUpsert.put("input", leads);
        String jsonString = objectMapper.writeValueAsString(leadsToUpsert);
        return runMarketoRequest(this::runUpsertStringInternal, jsonString);
    }

    @MarketoRetryable
    public LeadUpsertResponse runUpsertString(String leads) throws JsonProcessingException {
        return runMarketoRequest(this::runUpsertStringInternal, leads);
    }

    private String runUpsertStringInternal(Object request) {
        String jsonString = (String) request;
        return webClient
                .post()
                .uri(baseUrl + "/rest/v1/leads.json")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();
    }

    @MarketoRetryable
    public LeadUpsertResponse runLeadDeleteRequest(LeadDeleteRequest request) throws JsonProcessingException {
        return runMarketoRequest(this::runDelete, request);
    }

    private LeadUpsertResponse runMarketoRequest(JsonProcessingExceptionThrowingFunction<Object, String> fun, Object request) throws JsonProcessingException {
        LeadUpsertResponse lur;

        String jsonResult = fun.apply(request);
        lur = objectMapper.readValue(jsonResult, LeadUpsertResponse.class);
        checkForMarketoErrorResponses(lur);
        return lur;
    }

    private String runDelete(Object requestObj) throws JsonProcessingException {
        LeadDeleteRequest request = (LeadDeleteRequest) requestObj;

        if (ObjectUtils.isEmpty(request.getInput())) {
            throw new IllegalArgumentException("Input required");
        }

        String jsonString = objectMapper.writeValueAsString(request);

        return webClient
                .post()
                .uri(baseUrl + "/rest/v1/leads/delete.json")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();
    }

    private JavaType createParametricJavaType(Class<? extends LeadUpsertRecord> recordType) {
        JavaType jt = objectMapper.getTypeFactory().constructParametricType(LeadQueryResponse.class, recordType);
        LOGGER.info("Created new JavaType for recordType {}", recordType.getName());
        return jt;
    }

    // https://stackoverflow.com/a/18198349
    @FunctionalInterface
    public interface JsonProcessingExceptionThrowingFunction<T, R> {
        R apply(T t) throws JsonProcessingException;
    }
}
