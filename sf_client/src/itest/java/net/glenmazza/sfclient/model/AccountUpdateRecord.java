package net.glenmazza.sfclient.model;

/**
 * Example of using a class to alter a subset of an Entity's fields.
 * Fields not listed here will remain as-is after the update.
 */
public class AccountUpdateRecord {

    public int numberOfEmployees;
    public String site;

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }
}
