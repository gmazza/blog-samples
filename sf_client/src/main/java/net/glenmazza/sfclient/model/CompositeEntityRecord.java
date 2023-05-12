package net.glenmazza.sfclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Support for
 * <a href="https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/resources_composite_composite_post.htm">Composite calls</a>,
 * max of 25 subrequests per call.
 * For bulk inserts where each insert has no relation to the others MultipleEntityRecord is preferred, as it
 * has a 200 max per call.
 */
public abstract class CompositeEntityRecord {

    @JsonIgnore
    private final String entity;
    private final String referenceId;
    private final Method method;

    private String url;

    public CompositeEntityRecord(String entity, Method method, String referenceId) {
        this.entity = entity;
        this.method = method;
        this.referenceId = referenceId;
    }

    // entity represents the type of object the CRUD action applies to (Contact, Account, etc.)

    public String getEntity() {
        return entity;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public Method getMethod() {
        return method;
    }

    public enum Method {
        PATCH, // updates
        POST; // inserts
    }

}
