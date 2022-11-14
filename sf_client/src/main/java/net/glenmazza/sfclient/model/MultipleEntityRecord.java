package net.glenmazza.sfclient.model;

/**
 * For use with SalesforceMultipleRecordInserter, for bulk insertion of many records with one call.
 * @see SalesforceMultipleRecordInserter for details.
 *
 * Usage: Subclass with additional fields specific to the object you are creating, similar to EntityRecord.
 * Send POST call to Salesforce using MultipleEntityRecordRequest.
 */
public abstract class MultipleEntityRecord {

    private Attributes attributes;

    public MultipleEntityRecord(String type, String referenceId) {
        this.attributes = new Attributes(type, referenceId);
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public static class Attributes {
        private final String type;
        private final String referenceId;

        public Attributes(String type, String referenceId) {
            this.type = type;
            this.referenceId = referenceId;
        }

        public String getType() {
            return type;
        }

        public String getReferenceId() {
            return referenceId;
        }
    }
}
