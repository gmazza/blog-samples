package net.glenmazza.marketoclient.marketoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.CreateJobRequest;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.ExtractType;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.Job;
import net.glenmazza.marketoclient.marketoclient.model.bulkextract.JobStatusResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class BulkExtractService extends AbstractRESTService {

    public BulkExtractService(WebClient webClient, OAuth2AuthorizationFailureHandler failureHandler) {
        super(webClient, failureHandler);
    }

    // create job
    @Retryable(value = {MarketoAccessTokenExpiredException.class, MarketoTooFrequentRequestsException.class},
            maxAttempts = 4, backoff = @Backoff(delay = 2 * 1000, maxDelay = 62 * 1000))
    public JobStatusResponse createJob(CreateJobRequest request) throws JsonProcessingException {
        // need to specify lead or activity type
        if (request.getType() == null) {
            throw new IllegalStateException("Extract type missing!");
        } else if (request.getType().equals(ExtractType.LEADS) && request.getFilter().getActivityTypeIds() != null) {
            throw new IllegalStateException("Activity IDs valid only for activity extracts!");
        }

        // check each field name being overridden in the report was specified in the field list
        if (request.getColumnHeaderNames() != null) {
            Optional<String> unknownField = request.getColumnHeaderNames().keySet().stream().filter(ks -> request.getFields().contains(ks)).findFirst();
            if (unknownField.isPresent()) {
                throw new IllegalStateException("Column headers referring to unspecified field " + unknownField.get());
            }
        }

        String jsonRequest = objectMapper.writeValueAsString(request);

        JobStatusResponse jsr = webClient
                .post()
                .uri(baseUrl + String.format("/bulk/v1/%s/export/create.json",
                        request.getType().getEndpointValue()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonRequest)
                .retrieve()
                .bodyToMono(JobStatusResponse.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();

        checkForMarketoErrorResponses(jsr);
        return jsr;
    }

    public JobStatusResponse enqueueJob(ExtractType extractType, String exportId) {
        String uriSuffix = String.format("%s/export/%s/enqueue.json",
                extractType.getEndpointValue(), exportId);

        return apiCallForJobStatusResponse(HttpMethod.POST, uriSuffix);
    }

    public JobStatusResponse getJobStatus(ExtractType extractType, String exportId) {
        String uriSuffix = String.format("%s/export/%s/status.json",
                extractType.getEndpointValue(), exportId);

        return apiCallForJobStatusResponse(HttpMethod.GET, uriSuffix);
    }

    public JobStatusResponse getJobsByStatus(ExtractType extractType, List<Job.Status> statuses) throws JsonProcessingException {
        String statusCSV = statuses.stream()
                .map(Job.Status::getApiValue)
                .collect(Collectors.joining(","));

        String uriSuffix = String.format("%s/export.json?status=%s",
                extractType.getEndpointValue(), statusCSV);

        return apiCallForJobStatusResponse(HttpMethod.GET, uriSuffix);
    }

    // After enqueued and status COMPLETED, can now retrieve data
    // https://developers.marketo.com/rest-api/bulk-extract/bulk-activity-extract/#retrieving_your_data
    public String getFileData(ExtractType extractType, String exportId) throws IOException {
        // using simple String for the returned report, found InputStream per below article clunky:
        // https://www.springcloud.io/post/2022-07/spring-reactive-read-flux-into-inputstream/

        return webClient
                .get()
                .uri(baseUrl + String.format("/bulk/v1/%s/export/%s/file.json",
                                extractType.getEndpointValue(), exportId))
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();
    }


    @Retryable(value = {MarketoAccessTokenExpiredException.class, MarketoTooFrequentRequestsException.class},
        maxAttempts = 4, backoff = @Backoff(delay = 2 * 1000, maxDelay = 62 * 1000))
    private JobStatusResponse apiCallForJobStatusResponse(HttpMethod method, String uriSuffix) {
        JobStatusResponse jsr = webClient
            .method(method)
            .uri(baseUrl + "/bulk/v1/" + uriSuffix)
            .retrieve()
            .bodyToMono(JobStatusResponse.class)
            // retry of 1: if access token expired, will be removed after
            // first failed call and obtained & used during second.
            .retry(1)
            .block();

        // for debugging, can .bodyToMono(String.class), return String jsonResult
        // return objectMapper.readValue(jsonResult, JobStatusResponse.class);
        checkForMarketoErrorResponses(jsr);
        return jsr;
    }
}
