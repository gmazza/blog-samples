package net.glenmazza.marketoclient.marketoclient.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Items common to Marketo responses (<a href="https://developers.marketo.com/rest-api/error-codes/#response_level_errors">Marketo Docs</a>)
 */
public abstract class AbstractMarketoResponse {

    protected String requestId;
    protected boolean success;

    // populated when success = false
    protected List<ResponseError> errors;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<ResponseError> getErrors() {
        return errors;
    }

    public void setErrors(List<ResponseError> errors) {
        this.errors = errors;
    }

    @JsonIgnore
    public String getErrorSummary() {
        if (errors == null) {
            return "(no errors provided)";
        } else {
            return errors.stream().map(e -> e.getCode() + ": " + e.getMessage()).collect(Collectors.joining(","));
        }
    }

    @JsonIgnore
    public boolean tooManyRequestsError() {
        return getErrors() != null && getErrors().size() == 1 && "606".equals(getErrors().get(0).getCode());
    }
}
