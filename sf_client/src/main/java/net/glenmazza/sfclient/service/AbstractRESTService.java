package net.glenmazza.sfclient.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.sfclient.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRESTService.class);

    @Value("${salesforce.api.base-url}")
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

    protected JavaType createJavaType(Class<?> clazz) {
        JavaType jt = objectMapper.getTypeFactory().constructType(clazz);
        LOGGER.info("Created new JavaType for {}", clazz);
        return jt;
    }
}
