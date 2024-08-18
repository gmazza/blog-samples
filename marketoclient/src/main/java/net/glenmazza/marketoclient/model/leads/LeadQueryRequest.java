package net.glenmazza.marketoclient.model.leads;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * <a href="https://developers.marketo.com/rest-api/lead-database/leads/#query">Marketo API Docs for lead queries</a>
 */
public class LeadQueryRequest {

    private FilterType filterType;

    private List<String> filterValues;

    private List<String> fields;

    public FilterType getFilterType() {
        return filterType;
    }

    public void setFilterType(FilterType filterType) {
        this.filterType = filterType;
    }

    public List<String> getFilterValues() {
        return filterValues;
    }

    public void setFilterValues(List<String> filterValues) {
        this.filterValues = filterValues;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public enum FilterType {
        ID("id"), EMAIL("email");

        private final String fieldName;

        FilterType(String fieldName) {
            this.fieldName = fieldName;
        }

        // https://www.baeldung.com/jackson-serialize-enums#3-enums-and-jsonvalue
        @JsonValue
        public String getFieldName() {
            return fieldName;
        }
    }
}
