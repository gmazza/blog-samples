package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.sfclient.model.CompositeEntityRecord;
import net.glenmazza.sfclient.model.CompositeEntityRecordRequest;
import net.glenmazza.sfclient.model.CompositeEntityRecordResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
public class SalesforceCompositeRequestService extends AbstractRESTService {

    @Value("${salesforce.api.version:v57.0}")
    private String apiVersion;

    public SalesforceCompositeRequestService(WebClient.Builder webClientBuilder) {
        super();

        setWebClient(webClientBuilder
                .build());
    }

    public CompositeEntityRecordResponse bulkProcess(CompositeEntityRecordRequest cerr) throws JsonProcessingException {

        // create URLs for inserts
        cerr.getCompositeRequest().stream().filter(r -> CompositeEntityRecord.Method.POST.equals(r.getMethod())).forEach(
                ce -> ce.setUrl(String.format("/services/data/%s/sobjects/%s", apiVersion, ce.getEntity())));

        // create URLs for updates
        cerr.getCompositeRequest().stream().filter(r -> CompositeEntityRecord.Method.PATCH.equals(r.getMethod())).forEach(
                ce -> ce.setUrl(String.format("/services/data/%s/sobjects/%s/id/%s", apiVersion, ce.getEntity(), ce.getReferenceId())));

        String jsonString = objectMapper.writeValueAsString(cerr);

        return webClient
                .post()
                .uri(baseUrl + "/services/data/" + apiVersion + "/composite")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonString)
                .retrieve()
                .bodyToMono(CompositeEntityRecordResponse.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                // Can confirm by revoking token in Salesforce (Setup: Security: Session Management screen)
                // and comparing results vs. retry of 0
                .retry(1)
                .block();
    }

}
