package net.glenmazza.sfclient.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Service to call Apex REST endpoints:
 * https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_rest.htm
 */
@Service
public class ApexRESTCaller extends AbstractRESTService {

    @Value("${salesforce.api.base-url}")
    private String baseUrl;

    @Autowired
    public ApexRESTCaller(WebClient webClient) {
        super(webClient);
    }

    public String makeCall(String apiUrl, HttpMethod method, Map<String, Object> requestBody) {
        return webClient
                .method(method)
                .uri(baseUrl + "/" + apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(Mono.just(requestBody), Map.class)
                .retrieve()
                .bodyToMono(String.class)
                .retry(1)
                .block();
    }
}
