package net.glenmazza.domoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import net.glenmazza.domoclient.model.DataSetListRequest;
import net.glenmazza.domoclient.model.DataSetMetadata;
import net.glenmazza.domoclient.model.DataSetQueryRequest;
import net.glenmazza.domoclient.model.DataSetQueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataSetQueryRunner extends AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSetQueryRunner.class);

    private final Map<Class<?>, JavaType> parametricTypes = new HashMap<>();

    public DataSetQueryRunner(WebClient webClient) {
        super(webClient);
    }

    public DataSetQueryResponse runDataSetQuery(DataSetQueryRequest request)
            throws JsonProcessingException {

        String jsonResult = runQuery(request);
        return objectMapper.readValue(jsonResult, DataSetQueryResponse.class);
    }

    private String runQuery(DataSetQueryRequest request) throws JsonProcessingException {

        String sqlString = objectMapper.writeValueAsString(request.getQuery());

        return webClient
                .post()
                .uri(baseUrl + "/v1/datasets/query/execute/" + request.getDatasetId())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sqlString)
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();
    }

    // https://developer.domo.com/portal/72ae9b3e80374-list-data-sets
    public List<DataSetMetadata> getDataSetMetadata(DataSetListRequest request) throws JsonProcessingException {

        JavaType type = parametricTypes.computeIfAbsent(DataSetMetadata.class, this::createParametricJavaType);

        String jsonResult = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("api.domo.com")
                        .path("/v1/datasets")
                        .queryParam("limit", request.getLimit())
                        .queryParam("nameLike", request.getNameContains())
                        .queryParam("offset", request.getOffset())
                        .queryParam("sort", request.getSortBy().getName())
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();

        return objectMapper.readValue(jsonResult, type);
    }

    // https://developer.domo.com/portal/ea210df52a8a1-retrieve-a-data-set
    public DataSetMetadata getDataSetMetadataById(String datasetId) throws JsonProcessingException  {

        String jsonResult = webClient
                .get()
                .uri(baseUrl + "/v1/datasets/" + datasetId)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve()
                .bodyToMono(String.class)
                .retry(1)
                .block();
        return objectMapper.readValue(jsonResult, DataSetMetadata.class);
    }

    private JavaType createParametricJavaType(Class<?> clazz) {
        JavaType jt = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        LOGGER.info("Created new JavaType for class {}", clazz.getName());
        return jt;
    }

}
