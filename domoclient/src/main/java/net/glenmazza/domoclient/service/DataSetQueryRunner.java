package net.glenmazza.domoclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.glenmazza.domoclient.model.DataSetQueryRequest;
import net.glenmazza.domoclient.model.DataSetQueryResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;


public class DataSetQueryRunner extends AbstractRESTService {

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

}
