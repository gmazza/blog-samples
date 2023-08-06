package net.glenmazza.domoclient.model;

import java.util.List;

/**
 * Sample DataSet query response:
 *
 * {
 *   "datasource": "f0b09d28-97f9-4c0f-bed2-24750d62fa87",
 *   "columns": [
 *     "user_name",
 *     "user_id",
 *     "user_percentage"
 *   ],
 *   "metadata": [
 *     {
 *       "type": "STRING",
 *       "dataSourceId": "f0b09d28-97f9-4c0f-bed2-24750d62fa87",
 *       "maxLength": -1,
 *       "minLength": -1,
 *       "periodIndex": 0,
 *       "aggregated": false
 *     },
 *     ...
 *   ],
 *   "rows": [
 *     [
 *       "Bob Smith",
 *       "bobsmithid",
 *       19
 *     ],
 *     ...
 *   ],
 *   "numRows": 8,
 *   "numColumns": 3,
 *   "fromcache": false
 * }
 */
public class DataSetQueryResponse {

    // DataSet ID
    private String datasource;

    private List<String> columns;

    private List<Metadata> metadata;

    List<List<String>> rows;

    private int numRows;

    private int numColumns;

    boolean fromcache;

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(List<Metadata> metadata) {
        this.metadata = metadata;
    }

    public List<List<String>> getRows() {
        return rows;
    }

    public void setRows(List<List<String>> rows) {
        this.rows = rows;
    }

    public int getNumRows() {
        return numRows;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
    }

    public boolean isFromcache() {
        return fromcache;
    }

    public void setFromcache(boolean fromcache) {
        this.fromcache = fromcache;
    }

    public static class Metadata {
        String type;
        // datasource column came from
        String dataSourceId;
        // -1 if no defined max
        int maxLength;
        // -1 if no defined min
        int minLength;
        int periodIndex;
        boolean aggregated;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDataSourceId() {
            return dataSourceId;
        }

        public void setDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }

        public int getPeriodIndex() {
            return periodIndex;
        }

        public void setPeriodIndex(int periodIndex) {
            this.periodIndex = periodIndex;
        }

        public boolean isAggregated() {
            return aggregated;
        }

        public void setAggregated(boolean aggregated) {
            this.aggregated = aggregated;
        }
    }

}
