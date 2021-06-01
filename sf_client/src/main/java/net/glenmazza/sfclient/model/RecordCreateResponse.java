package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Class holds the response of creating a Salesforce record:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_sobject_create.htm
 */
public class RecordCreateResponse {
    String id;
    boolean success;
    List<String> errors;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
