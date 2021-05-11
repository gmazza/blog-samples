package net.glenmazza.cdclistener.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

/**
 * See here for Salesforce guide on Change Data Capture:
 * https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_intro.htm
 * https://trailhead.salesforce.com/en/content/learn/trails/design-eventdriven-apps-for-realtime-integration
 */
public class BaseCDCEvent {

    private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public static class Event {
        long replayId;

        public long getReplayId() {
            return replayId;
        }

        public void setReplayId(long replayId) {
            this.replayId = replayId;
        }
    }

    public static class BasePayload {

        private ChangeEventHeader changeEventHeader;

        public ChangeEventHeader getChangeEventHeader() {
            return changeEventHeader;
        }

        public void setChangeEventHeader(ChangeEventHeader changeEventHeader) {
            this.changeEventHeader = changeEventHeader;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class ChangeEventHeader {
            public enum ChangeType { CREATE, UPDATE, DELETE, UNDELETE, GAP_CREATE, GAP_UPDATE, GAP_DELETE, GAP_UNDELETE,
                GAP_OVERFLOW };

            String entityName;
            ChangeType changeType;
            List<String> changedFields;
            Instant commitTimestamp;
            List<String> recordIds;

            public String getEntityName() {
                return entityName;
            }

            public void setEntityName(String entityName) {
                this.entityName = entityName;
            }

            public ChangeType getChangeType() {
                return changeType;
            }

            public void setChangeType(ChangeType changeType) {
                this.changeType = changeType;
            }

            public List<String> getChangedFields() {
                return changedFields;
            }

            public void setChangedFields(List<String> changedFields) {
                this.changedFields = changedFields;
            }

            public Instant getCommitTimestamp() {
                return commitTimestamp;
            }

            public void setCommitTimestamp(Instant commitTimestamp) {
                this.commitTimestamp = commitTimestamp;
            }

            public List<String> getRecordIds() {
                return recordIds;
            }

            public void setRecordIds(List<String> recordIds) {
                this.recordIds = recordIds;
            }
        }

    }

}
