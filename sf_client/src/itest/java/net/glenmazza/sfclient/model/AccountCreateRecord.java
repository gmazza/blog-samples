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
public class AccountCreateRecord {
    public enum RatingEnum { Hot, Warm, Cold };

    String site;
    String name;
    int numberOfEmployees;
    RatingEnum rating;

    @JsonProperty("SLAExpirationDate__c")
    LocalDate slaExpirationDate;

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
    public LocalDate getSLAExpirationDate() {
        return slaExpirationDate;
    }

    public void setSLAExpirationDate(LocalDate slaExpirationDate) {
        this.slaExpirationDate = slaExpirationDate;
    }
}
