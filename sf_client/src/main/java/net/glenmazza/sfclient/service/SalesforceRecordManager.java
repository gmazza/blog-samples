package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.sfclient.model.RecordCreateResponse;
import net.glenmazza.sfclient.model.SOQLQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to perform CRUD operations on Salesforce records:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/using_resources_working_with_records.htm
 */
@Service
public class SalesforceRecordManager extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceRecordManager.class);

    public SalesforceRecordManager(WebClient webClient) {
        super(webClient);
    }

    @Value("${salesforce.api.version:v50.0}")
    private String apiVersion;

    private final Map<Class<?>, JavaType> javaTypeMap = new HashMap<>();

    /**
     * Update a record in the Salesforce Database using a POJO.
     * WARNING: Any null field in the POJO will be erased in Salesforce, if updating just a few
     * fields use an object containing just those fields, or supply a Map<String, Object> instead.
     *
     * @param object: Class of Salesforce entity (Account, Contact, User, etc.) being updated
     * @param salesforceId: ID of Salesforce record being updated
     * @param updateObject: Class or Map containing JUST those fields that are to be updated.
     */
    public void updateByObject(String object, String salesforceId, Object updateObject) {

        webClient
                .patch()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + object + "/" + salesforceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(updateObject)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Create a record in the Salesforce Database using a POJO or a Map<String, Object>.
     * The columns of the POJO must match those of the field names for the Salesforce entity,
     * advisable to use @JsonProperty("emailaddress__c"), etc. annotations
     *
     * @param object: Class of Salesforce entity (Account, Contact, User, etc.) being created
     * @param insertObject: Class or Map containing all fields required for the object
     * @return RecordCreateResponse: Salesforce object containing create results (success or error)
     */
    public RecordCreateResponse createObject(String object, Object insertObject) throws JsonProcessingException {

        // first parsing to JSON to avoid problems working with LocalDates.
        String jsonString = objectMapper.writeValueAsString(insertObject);

        return webClient
                .post()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + object)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(RecordCreateResponse.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    /**
     * Delete a record in the Salesforce Database by its Salesforce Id.
     *
     * @param object: Class of Salesforce entity (Account, Contact, User, etc.) being deleted
     * @param salesforceId: ID of Salesforce record being delete
     */
    public void deleteObject(String object, String salesforceId) {
        webClient.delete()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + object + "/" + salesforceId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .toBodilessEntity()
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    public String getJson(String object, String salesforceId) {
        return webClient
                .get()
                .uri(baseUrl + "/services/data/" + apiVersion + "/sobjects/" + object + "/" + salesforceId)
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

    public <T> T getObject(String object, String salesforceId, Class<?> returnedObjectType) throws JsonProcessingException {
        JavaType type = javaTypeMap.computeIfAbsent(returnedObjectType, this::createJavaType);
        String jsonResult = getJson(object, salesforceId);
        return objectMapper.readValue(jsonResult, type);
    }

    private JavaType createJavaType(Class<?> clazz) {
        JavaType jt = objectMapper.getTypeFactory().constructType(clazz);
        LOGGER.info("Created new JavaType for {}", clazz);
        return jt;
    }

}
