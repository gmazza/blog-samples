package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Class holds the results, up to a maximum, usually 2000, of a SOQL query:
 * https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/dome_query.htm
 * <p>
 * In case of queries going beyond the maximum, the done field will be false and the
 * nextRecordsUrl populated, for which SOQLQueryRunner's runNextQuery() can be called until
 * all retrieved.
 * <p>
 * The EntityRecord object is the only portion that is dependent on what you are querying,
 * and will need to be implemented with fields to hold those values.  The model folder in the
 * itest directory offers some examples of subclassing this object.
 */
public class SOQLQueryResponse<T extends EntityRecord> {

    // Total number of records that the query and any successive calls to SQLQueryRunner.nextRecordsUrl()
    // will return.  For the number returned in any individual call, use getRecords().size().
    int totalSize;

    // If false, there are more batches to query.
    boolean done;

    // Meaningful only if done=false.  Use SQLQueryRunner.runNextQuery(nextRecordsUrl) to obtain next batch.
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
