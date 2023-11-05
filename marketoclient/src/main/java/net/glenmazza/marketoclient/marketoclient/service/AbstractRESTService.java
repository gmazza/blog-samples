package net.glenmazza.marketoclient.marketoclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.marketoclient.marketoclient.util.JSONUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractRESTService {

    @Value("${marketo.api.base-url}")
    protected String baseUrl;

    protected WebClient webClient;

    protected final ObjectMapper objectMapper;

    public AbstractRESTService() {
        this.objectMapper = JSONUtils.createObjectMapper();
    }

    protected AbstractRESTService(WebClient webClient) {
        this();
        this.webClient = webClient;
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

}
