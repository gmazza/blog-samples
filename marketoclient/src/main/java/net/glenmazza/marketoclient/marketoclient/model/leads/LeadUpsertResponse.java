package net.glenmazza.marketoclient.marketoclient.model.leads;

import com.fasterxml.jackson.annotation.JsonValue;
import net.glenmazza.marketoclient.marketoclient.model.AbstractMarketoResponse;
import net.glenmazza.marketoclient.marketoclient.model.ResponseError;

import java.util.List;

/**
 * See response in
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#create_and_update">Create and Update Section</a>
 * of Marketo docs for an example.
 * This object also returned when deleting leads.
 */
public class LeadUpsertResponse extends AbstractMarketoResponse {

    private List<Result> result;

    public List<Result> getResult() {
        return result;
    }

    public void setResult(List<Result> result) {
        this.result = result;
    }

    public static class Result {
        private int id;
        private Status status;

        private List<ResponseError> reasons;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public List<ResponseError> getReasons() {
            return reasons;
        }

        public void setReasons(List<ResponseError> reasons) {
            this.reasons = reasons;
        }

        /* experience so far, reasons non-null only if status = SKIPPED */
        public enum Status {
            CREATED("created"), UPDATED("updated"), DELETED("deleted"), SKIPPED("skipped");

            private final String apiValue;

            Status(String apiValue) {
                this.apiValue = apiValue;
            }

            @JsonValue
            public String getApiValue() {
                return apiValue;
            }
        }

    }

}
