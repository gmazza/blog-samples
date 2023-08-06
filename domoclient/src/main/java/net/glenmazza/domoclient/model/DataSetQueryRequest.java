package net.glenmazza.domoclient.model;


/**
 * <a href="https://developer.domo.com/portal/d9520f5752d56-get-access-token">Domo information on getAccessToken</a>
 * Note <a href="https://community-forums.domo.com/main/discussion/comment/39426#Comment_39426">no built-in support</a>
 * for pagination, for very large datasets can possibly rely on WHERE clause in SQL statement to approximate it.
 */
public class DataSetQueryRequest {

    public record Query(String sql) { }

    public DataSetQueryRequest(String datasetId, String sql) {
        this.datasetId = datasetId;
        this.query = new Query(sql);
    }

    // Below not recommended, best to specify columns needed to reduce data returned.
    // Also, if you use, will need to rely on column specification
    // in response to see the ordering of columns returned (although probably same
    // as ordering of columns in dataset.)
    public DataSetQueryRequest(String datasetId) {
        this.datasetId = datasetId;
        this.query = new Query("select * from table");
    }

    private final String datasetId;

    private final Query query;

    public String getDatasetId() {
        return datasetId;
    }

    public Query getQuery() {
        return query;
    }

}
