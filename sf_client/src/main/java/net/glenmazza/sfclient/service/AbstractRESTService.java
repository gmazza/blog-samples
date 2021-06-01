package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.util.JSONUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractRESTService {

    @Value("${salesforce.api.base-url}")
    protected String baseUrl;

    protected final WebClient webClient;

    protected final ObjectMapper objectMapper;

    protected AbstractRESTService(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = JSONUtils.createObjectMapper();
    }
}
