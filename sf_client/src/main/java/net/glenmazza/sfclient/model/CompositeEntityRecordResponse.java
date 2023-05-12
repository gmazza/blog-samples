package net.glenmazza.sfclient.model;

import java.util.List;
import java.util.Map;

public class CompositeEntityRecordResponse {

    List<Result> compositeResponse;

    public List<Result> getCompositeResponse() {
        return compositeResponse;
    }

    public void setCompositeResponse(List<Result> compositeResponse) {
        this.compositeResponse = compositeResponse;
    }

    public static class Result {
        private int httpStatusCode;
        private String referenceId;

        private Object body;

        private Map<String, String> httpHeaders;

        public int getHttpStatusCode() {
            return httpStatusCode;
        }

        public void setHttpStatusCode(int httpStatusCode) {
            this.httpStatusCode = httpStatusCode;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public Map<String, String> getHttpHeaders() {
            return httpHeaders;
        }

        public void setHttpHeaders(Map<String, String> httpHeaders) {
            this.httpHeaders = httpHeaders;
        }

        public Object getBody() {
            return body;
        }

        public void setBody(Object body) {
            this.body = body;
        }

        public Map<String, Object> getSuccessResultsMap() {
            // aware only of 200 and 201 codes for now
            if (!(getHttpStatusCode() == 200 || getHttpStatusCode() == 201)) {
                throw new IllegalArgumentException(
                        String.format("Result HttpStatusCode %s is not 200 or 201, call getErrorResultsList?", getHttpStatusCode()));
            }

            return (Map<String, Object>) getBody();
        }

        public List<Map<String, Object>> getErrorResultsList() {
            if (getHttpStatusCode() == 200 || getHttpStatusCode() == 201) {
                throw new IllegalArgumentException(
                        String.format("Result HttpStatusCode %s indicates success, call getSuccessResultsMap?", getHttpStatusCode()));
            }

            return (List<Map<String, Object>>) getBody();
        }

    }
}
