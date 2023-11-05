package net.glenmazza.marketoclient.marketoclient.model.leads;

/**
 * For holding the results of a Lead query (seeing the fields desired returned)
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#query">Marketo Docs</a>
 * Note id field always returned, even if not specified in retrieval list, so id added to this class.
 * The fields returned can be default fields, or customized if specified in the LeadQueryRequest.
 * If you customize, you'll need to subclass this class to provide fields in the result.
 * If you're not customizing, use DefaultLeadQueryResultRecord for the default fields returned by Marketo.
 **/
public abstract class LeadQueryRecord {

    int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
