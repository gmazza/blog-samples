package net.glenmazza.marketoclient.marketoclient.model.leads;

import net.glenmazza.marketoclient.marketoclient.model.AbstractMarketoResponse;

import java.util.List;

/**
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#query">Marketo Docs - Querying Leads</a>
 */
public class LeadQueryResponse<T extends LeadUpsertRecord> extends AbstractMarketoResponse {

    List<T> result;

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

}
