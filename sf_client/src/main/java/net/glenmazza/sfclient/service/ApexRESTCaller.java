package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service to call Apex REST endpoints:
 * https://developer.salesforce.com/docs/atlas.en-us.apexcode.meta/apexcode/apex_rest.htm
 */
@Service
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
public class ApexRESTCaller extends AbstractRESTService {

    private final Map<Class<?>, JavaType> javaTypeMap = new HashMap<>();

    private final String apexUrl;

    @Autowired
    public ApexRESTCaller(WebClient webClient, @Value("${salesforce.api.base-url}") String baseUrl) {
        super(webClient);
        apexUrl = baseUrl + "/services/apexrest/";
    }

    public String makeCall(String apiUrl, HttpMethod method) {
        return makeCall(apiUrl, method, null);
    }

    public String makeCall(String apiUrl, HttpMethod method, Map<String, Object> requestBody) {
        var requestBodySpec = webClient
                .method(method)
                .uri(apexUrl + apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (requestBody != null) {
            requestBodySpec.body(Mono.just(requestBody), Map.class);
        }
        return requestBodySpec.retrieve()
                .bodyToMono(String.class)
                .retry(1)
                .block();
    }

    public String get(String apiUrl) {
        return webClient
                .get()
                .uri(apexUrl + apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .retry(1)
                .block();
    }

    public <T> T getObject(String apiUrl, Class<? extends T> clazz) throws JsonProcessingException {
        String jsonResult = get(apiUrl);
        JavaType type = javaTypeMap.computeIfAbsent(clazz, this::createJavaType);
        return objectMapper.readValue(jsonResult, type);
    }

}
