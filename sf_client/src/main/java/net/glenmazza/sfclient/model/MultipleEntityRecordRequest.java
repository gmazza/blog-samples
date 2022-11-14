package net.glenmazza.sfclient.model;

import java.util.List;

/**
 * Use this object to store the array of items being inserted into Salesforce
 * @see SalesforceMultipleRecordInserter for sample request and response JSON.
 *
 * @param <T> The MultipleEntityRecord subclass holding the fields for one item that will be placed into Salesforce
 */
public class MultipleEntityRecordRequest<T extends MultipleEntityRecord> {

    List<T> records;

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
