package net.glenmazza.domoclient.model;

// https://developer.domo.com/portal/72ae9b3e80374-list-data-sets
public class DataSetListRequest {

    // case-insensitive
    private String nameContains = "";
    private SortField sortBy = SortField.NAME;

    // index into query, once past total number of datasets, 0 are returned
    private int offset = 0;

    // max is 50
    private int limit = 50;

    public DataSetListRequest() {
    }

    public DataSetListRequest(String nameContains) {
        this.nameContains = nameContains;
    }

    public DataSetListRequest(String nameContains, SortField sortBy, int offset, int limit) {
        this.nameContains = nameContains;
        this.sortBy = sortBy;
        this.offset = offset;
        this.limit = limit;
        // 50 is limit specified from Domo docs
        assert (1 <= limit && limit <= 50);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        assert (1 <= limit && limit <= 50);
        this.limit = limit;
    }

    public String getNameContains() {
        return nameContains;
    }

    public void setNameContains(String nameContains) {
        this.nameContains = nameContains;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public SortField getSortBy() {
        return sortBy;
    }

    public void setSortBy(SortField sortBy) {
        this.sortBy = sortBy;
    }

    public enum SortField {
        // only two that seem to be supported, if none given then return is unsorted
        NAME("name"),
        CREATED_AT_DESC("createdAt");

        private final String name;

        SortField(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    };

}
