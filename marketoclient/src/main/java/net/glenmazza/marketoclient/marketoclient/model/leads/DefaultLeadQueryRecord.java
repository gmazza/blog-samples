package net.glenmazza.marketoclient.marketoclient.model.leads;

import java.util.Date;

/**
 * Default lead queries return these fields.  This object can be used in the LeadQueryResponse
 * if you're not customizing the fields returned, subclass LeadQueryResultRecord per its comments
 * otherwise.
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#query">Marketo Docs</a>
 */
public class DefaultLeadQueryRecord extends LeadQueryRecord {
    private Date updatedAt;
    private String lastName;
    private String email;
    private Date createdAt;
    private String firstName;

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
}
