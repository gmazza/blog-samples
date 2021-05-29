package net.glenmazza.sfclient.model;

/**
 * Concrete implementation of SOQLQueryResponse.Record used to
 * support a particular SOQL query for Account objects.
 */
public class AccountRecord extends SOQLQueryResponse.Record {
    String name;
    String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
