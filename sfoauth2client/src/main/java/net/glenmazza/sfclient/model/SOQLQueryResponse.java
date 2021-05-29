package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Class holds the results of a SOQL query:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
 *
 * The Record object is the only portion that is dependent on what you are are querying,
 * and will need to be implemented with fields to hold those values, see AccountRecord
 * in the itest folder for an example.
 */
public class SOQLQueryResponse<T extends SOQLQueryResponse.Record> {
    int totalSize;
    boolean done;
    String nextRecordsUrl;

    List<T> records;

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public String getNextRecordsUrl() {
        return nextRecordsUrl;
    }

    public void setNextRecordsUrl(String nextRecordsUrl) {
        this.nextRecordsUrl = nextRecordsUrl;
    }

    public static class Record {
        Attributes attributes;

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes attributes) {
            this.attributes = attributes;
        }

        public static class Attributes {
            String type;
            String url;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
        }
    }
}
