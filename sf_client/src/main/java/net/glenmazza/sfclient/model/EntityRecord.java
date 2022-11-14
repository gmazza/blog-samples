package net.glenmazza.sfclient.model;

/**
 * Subclasses of this object have two usages:
 * 1.) Hold the requested fields for an Entity (like Account, Contact, etc.) in a SOQL Query,
 *     within a SOQLQueryResponse
 * 2.) Holder of information when an entity is returned from an Apex stored procedure.
 *
 * Usage:
 * 1.) Subclass and add fields like any POJO for each column of the entity to be returned.
 * 2.) Add @JsonIgnoreProperties(ignoreUnknown = true) if there are columns being returned that
 *     you haven't provided fields for.
 */
public abstract class EntityRecord {

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
