package net.glenmazza.sfclient.model;

/**
 * Concrete implementation of SOQLQueryResponse.Record used to
 * support a particular SOQL query for Account objects.
 */
public class AccountQueryRecord extends SOQLQueryResponse.Record {
    String name;
    String site;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
