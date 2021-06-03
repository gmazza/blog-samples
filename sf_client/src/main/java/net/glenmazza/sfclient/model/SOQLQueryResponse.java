package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Class holds the results of a SOQL query:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
 *
 * The EntityRecord object is the only portion that is dependent on what you are are querying,
 * and will need to be implemented with fields to hold those values.  The model folder in the
 * itest directory offers some examples of subclassing this object.
 */
public class SOQLQueryResponse<T extends EntityRecord> {
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

}
