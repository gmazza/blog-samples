package net.glenmazza.sfclient.model;

import java.util.List;

public class CompositeEntityRecordRequest {

    // allOrNone: if true, error in one causes rollback of all.
    // https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/requests_composite.htm
    // requiring in constructor as it is an important property
    public CompositeEntityRecordRequest(boolean allOrNone) {
        this.allOrNone = allOrNone;
    }

    List<? extends CompositeEntityRecord> compositeRequest;

    //
    boolean allOrNone;

    public List<? extends CompositeEntityRecord> getCompositeRequest() {
        return compositeRequest;
    }

    public void setCompositeRequest(List<? extends CompositeEntityRecord> compositeRequest) {
        this.compositeRequest = compositeRequest;
    }

    public boolean isAllOrNone() {
        return allOrNone;
    }

}
