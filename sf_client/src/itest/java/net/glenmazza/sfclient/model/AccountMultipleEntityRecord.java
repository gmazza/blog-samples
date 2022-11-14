package net.glenmazza.sfclient.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Sample Java class used to insert an Account into Salesforce.
 * All possible Account views here:
 * https://...yoursalesforceURL.../lightning/setup/ObjectManager/Account/FieldsAndRelationships/view
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountMultipleEntityRecord extends MultipleEntityRecord {
    public enum RatingEnum { Hot, Warm, Cold };

    private static final String SF_OBJECT_TYPE = "Account";

    String site;
    String name;
    int numberOfEmployees;
    RatingEnum rating;

    public AccountMultipleEntityRecord(String referenceId) {
        super(SF_OBJECT_TYPE, referenceId);
    }

    @JsonProperty("Lead_Date__c")
    LocalDate leadDate;

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(int numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    public RatingEnum getRating() {
        return rating;
    }

    public void setRating(RatingEnum rating) {
        this.rating = rating;
    }

    @JsonIgnore
    public LocalDate getLeadDate() {
        return leadDate;
    }

    public void setLeadDate(LocalDate leadDate) {
        this.leadDate = leadDate;
    }
}
