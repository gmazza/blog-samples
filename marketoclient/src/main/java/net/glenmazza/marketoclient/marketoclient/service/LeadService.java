package net.glenmazza.marketoclient.marketoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadDeleteRequest;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadQueryRequest;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadQueryResponse;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadUpsertRecord;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadUpsertRequest;
import net.glenmazza.marketoclient.marketoclient.model.leads.LeadUpsertResponse;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    public LeadService(WebClient webClient) {
        super(webClient);
    }

    // needed for Jackson deserialization of parametric type used in LeadQueryResponse
    private final Map<Class<? extends LeadUpsertRecord>, JavaType> parametricTypes = new HashMap<>();

    public <T extends LeadUpsertRecord> LeadQueryResponse<T> runLeadQueryRequest(LeadQueryRequest request,
                                                                                 Class<? extends LeadUpsertRecord> recordType)
            throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(recordType, this::createParametricJavaType);
        String jsonResult = runQuery(request);
        return objectMapper.readValue(jsonResult, type);
    }

    private String runQuery(LeadQueryRequest request) throws JsonProcessingException {

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

    public LeadUpsertResponse runLeadUpsertRequest(LeadUpsertRequest<?> request) throws JsonProcessingException {
        String jsonResult = runUpsert(request);
        return objectMapper.readValue(jsonResult, LeadUpsertResponse.class);
    }

    private String runUpsert(LeadUpsertRequest<?> request) throws JsonProcessingException {
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
        return runUpsertStringInternal(jsonString);
    }

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
        String jsonResult = runUpsertStringInternal(jsonString);
        return objectMapper.readValue(jsonResult, LeadUpsertResponse.class);
    }

    public LeadUpsertResponse runUpsertString(String leads) throws JsonProcessingException {
        String jsonResult = runUpsertStringInternal(leads);
        return objectMapper.readValue(jsonResult, LeadUpsertResponse.class);
    }

    public String runUpsertStringInternal(String jsonString) throws JsonProcessingException {
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

    public LeadUpsertResponse runLeadDeleteRequest(LeadDeleteRequest request) throws JsonProcessingException {
        String jsonResult = runDelete(request);
        return objectMapper.readValue(jsonResult, LeadUpsertResponse.class);
    }

    private String runDelete(LeadDeleteRequest request) throws JsonProcessingException {

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

}
