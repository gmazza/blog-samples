package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Object returned when all insertions have been successful.
 * @see SalesforceMultipleRecordInserter for sample request and response JSON
 */
public class MultipleEntityRecord201Response {

    // should always be false with a 201 response, otherwise
    // MultipleEntityRecord400Response usually returned
    private boolean hasErrors;
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public static class Result {
        private String referenceId;

        private String id;

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
