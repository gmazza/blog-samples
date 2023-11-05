package net.glenmazza.marketoclient.marketoclient.model.activities;

import net.glenmazza.marketoclient.marketoclient.model.AbstractMarketoResponse;

import java.util.List;

public class ActivityTypeResponse extends AbstractMarketoResponse {

    private boolean moreResult;
    private String nextPageToken;
    private List<ActivityType> result;

    public boolean isMoreResult() {
        return moreResult;
    }

    public void setMoreResult(boolean moreResult) {
        this.moreResult = moreResult;
    }

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<ActivityType> getResult() {
        return result;
    }

    public void setResult(List<ActivityType> result) {
        this.result = result;
    }

    public static class ActivityType {
        private int id;
        private String apiName;
        private String description;
        private List<ActivityTypeAttribute> attributes;
        private String name;
        private ActivityTypeAttribute primaryAttribute;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<ActivityTypeAttribute> getAttributes() {
            return attributes;
        }

        public void setAttributes(List<ActivityTypeAttribute> attributes) {
            this.attributes = attributes;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public ActivityTypeAttribute getPrimaryAttribute() {
            return primaryAttribute;
        }

        public void setPrimaryAttribute(ActivityTypeAttribute primaryAttribute) {
            this.primaryAttribute = primaryAttribute;
        }
    }

    public static class ActivityTypeAttribute {
        private String apiName;
        private String dataType;
        private String name;

        public String getApiName() {
            return apiName;
        }

        public void setApiName(String apiName) {
            this.apiName = apiName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
