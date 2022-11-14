package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.sfclient.model.MultipleEntityRecord201Response;
import net.glenmazza.sfclient.model.MultipleEntityRecord400ResponseException;
import net.glenmazza.sfclient.model.MultipleEntityRecordRequest;
import net.glenmazza.sfclient.model.ServiceException;
import net.glenmazza.sfclient.oauth2.WebClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Intended for bulk inserts into Salesforce, with just one API call consumed for up to 200 records max. SF docs:
 * https://developer.salesforce.com/docs/atlas.en-us.240.0.api_rest.meta/api_rest/dome_composite_sobject_tree_flat.htm
 *
 * Sample call:
 * https://developer.salesforce.com/docs/atlas.en-us.240.0.api_rest.meta/api_rest/dome_composite_sobject_tree_flat.htm
 * Note attribute type and unique referenceID (can be any value, just needs to be unique per call) *required*
 *
 * See test cases for basic operation.
 *
 * Success 201 Created response gives SF IDs of created objects per reference ID provided above:
 * Item viewable in Salesforce UI by plugging in an id below, e.g.:
 * https://yourinstance.lightning.force.com/a882i0000008cWAAAI
 * {
 *     "hasErrors": false,
 *     "results": [
 *         {
 *             "referenceId": "anyUniqueID1",
 *             "id": "a882i0000008cWAAAI"
 *         },
 *         {
 *             "referenceId": "anyUniqueID2",
 *             "id": "a882i0000008cWAAAB"
 *         }
 *     ]
 * }
 *
 * Generic error response 403 if reference IDs missing:
 * [
 *     {
 *         "message": "Include a reference ID for each record in the request.",
 *         "errorCode": "INVALID_INPUT"
 *     }
 * ]
 *
 * Usual error response 400 otherwise:
 *
 * {
 *     "hasErrors": true,
 *     "results": [
 *         {
 *             "referenceId": "a882i0000008cWAAAI",
 *             "errors": [
 *                 {
 *                     "statusCode": "INVALID_INPUT",
 *                     "message": "Duplicate ReferenceId provided in the request.",
 *                     "fields": []
 *                 }
 *             ]
 *         },
 *         {
 *             "referenceId": "a882i0000008cWAAAB",
 *             "errors": [
 *                 {
 *                     "statusCode": "INVALID_INPUT",
 *                     "message": "Include an entity type for each record in the request.",
 *                     "fields": []
 *                 }
 *             ]
 *         }
 *     ]
 * }
 */
@Service
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
public class SalesforceMultipleRecordInserter extends AbstractRESTService {

    private static final Logger LOG = LoggerFactory.getLogger(SalesforceMultipleRecordInserter.class);

    @Value("${salesforce.api.version:v56.0}")
    private String apiVersion;

    public SalesforceMultipleRecordInserter(WebClient.Builder webClientBuilder) {
        super();

        setWebClient(webClientBuilder
                .filter(WebClientFilter.handleErrors(this::getMonoClientResponse))
                .build());
    }

    public MultipleEntityRecord201Response bulkInsert(String entity, MultipleEntityRecordRequest<?> merr) throws JsonProcessingException {

        String jsonString = objectMapper.writeValueAsString(merr);

        return webClient
                .post()
                .uri(baseUrl + "/services/data/" + apiVersion + "/composite/tree/" + entity)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(MultipleEntityRecord201Response.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

    public Mono<ClientResponse> getMonoClientResponse(ClientResponse response) {
        HttpStatus status = response.statusCode();

        if (BAD_REQUEST.equals(status)) {
            return response.bodyToMono(MultipleEntityRecord400ResponseException.Response.class)
                    .flatMap(body -> Mono.error(new MultipleEntityRecord400ResponseException(body)));
        } else {
            return response.bodyToMono(String.class)
                    // defaultIfEmpty:  401's, 403's, etc. sometimes return null body
                    // https://careydevelopment.us/blog/spring-webflux-how-to-handle-empty-responses
                    .defaultIfEmpty(status.getReasonPhrase())
                    .flatMap(body -> {
                        LOG.info("Error status code {} ({}) Response Body: {}", status.value(),
                                status.getReasonPhrase(), body);
                        // return Mono.just(response); <-- throws WebClient exception back to client instead
                        return Mono.error(new ServiceException(body, response.rawStatusCode()));
                    });
        }
    }

}
