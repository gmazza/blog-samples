package net.glenmazza.marketoclient.model.bulkextract;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExtractType {
    LEADS("leads"), ACTIVITIES("activities");

    private final String endpointValue;

    @JsonValue
    public String getEndpointValue() {
        return endpointValue;
    }

    ExtractType(String endpointValue) {
        this.endpointValue = endpointValue;
    }
}
