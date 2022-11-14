package net.glenmazza.sfclient.model;


import java.util.List;

/**
 * Exception returned when some insertions had problems.  In case of any error, none of the other
 * insertions will go through, so recovery process may involve:
 * 1.) logging the errors
 * 2.) removing the bad insertions from the original request (matching on reference ID)
 * 3.) re-sending the original request minus the problematic records so they will insert.
 *
 * @see SalesforceMultipleRecordInserter for sample request and response JSON
 */
public class MultipleEntityRecord400ResponseException extends RuntimeException {

    private final Response response;

    public MultipleEntityRecord400ResponseException(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return response;
    }

    public static class Response {

        // should always be true with a 400 response
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
            private List<Error> errors;

            public String getReferenceId() {
                return referenceId;
            }

            public void setReferenceId(String referenceId) {
                this.referenceId = referenceId;
            }

            public List<Error> getErrors() {
                return errors;
            }

            public void setErrors(List<Error> errors) {
                this.errors = errors;
            }

            public static class Error {
                private String statusCode;
                private String message;
                private List<String> fields;

                public String getStatusCode() {
                    return statusCode;
                }

                public void setStatusCode(String statusCode) {
                    this.statusCode = statusCode;
                }

                public String getMessage() {
                    return message;
                }

                public void setMessage(String message) {
                    this.message = message;
                }

                public List<String> getFields() {
                    return fields;
                }

                public void setFields(List<String> fields) {
                    this.fields = fields;
                }
            }

        }
    }

}
