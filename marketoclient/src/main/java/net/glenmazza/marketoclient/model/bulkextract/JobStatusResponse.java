package net.glenmazza.marketoclient.model.bulkextract;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.glenmazza.marketoclient.model.AbstractMarketoResponse;

import java.util.List;

/**
 *  Common response object when working with Bulk Extract Jobs.
 *  <a href="https://developers.marketo.com/rest-api/bulk-extract/">Marketo Docs</a>
 */
public class JobStatusResponse extends AbstractMarketoResponse {

    // populated if success == true
    private List<Job> result;

    public List<Job> getResult() {
        return result;
    }

    public void setResult(List<Job> result) {
        this.result = result;
    }

    @JsonIgnore
    public Job.Status getExpectedSingleJobStatus() {
        if (!isSuccess()) {
            throw new IllegalStateException("Marketo call did not successfully complete: " + getErrorSummary());
        }

        if (result == null || result.size() != 1) {
            throw new IllegalStateException("Expected single job returned, got " + ((result == null) ? "0" : result.size()));
        }
        Job.Status status = getResult().get(0).getStatus();
        if (status == null) {
            throw new IllegalStateException("Unexpected null status returned from Marketo");
        }
        return status;
    }

}
