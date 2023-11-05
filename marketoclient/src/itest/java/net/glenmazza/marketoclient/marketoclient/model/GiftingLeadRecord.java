package net.glenmazza.marketoclient.marketoclient.model;

import net.glenmazza.marketoclient.marketoclient.model.leads.LeadUpsertRecord;

import java.util.Date;

public class GiftingLeadRecord extends LeadUpsertRecord {

    private String email;
    private Integer peekabooSharingPROArticles;
    private Date peekabooSharingLastDatePROArticle;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getPeekabooSharingPROArticles() {
        return peekabooSharingPROArticles;
    }

    public void setPeekabooSharingPROArticles(Integer peekabooSharingPROArticles) {
        this.peekabooSharingPROArticles = peekabooSharingPROArticles;
    }

    public Date getPeekabooSharingLastDatePROArticle() {
        return peekabooSharingLastDatePROArticle;
    }

    public void setPeekabooSharingLastDatePROArticle(Date peekabooSharingLastDatePROArticle) {
        this.peekabooSharingLastDatePROArticle = peekabooSharingLastDatePROArticle;
    }
}
