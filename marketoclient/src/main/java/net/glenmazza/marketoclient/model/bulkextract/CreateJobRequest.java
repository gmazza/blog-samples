package net.glenmazza.marketoclient.model.bulkextract;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * <a href="https://developers.marketo.com/rest-api/bulk-extract/#creating_a_job">Marketo: Creating a Job</a>
 * <a href="https://developers.marketo.com/rest-api/bulk-extract/bulk-activity-extract/#creating_a_job">See Also</a>
 */
public class CreateJobRequest {

    private static final DateTimeFormatter DATES_FROM_MARKETO_FORMAT
            = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public CreateJobRequest(ExtractType type, ExtractFormat format) {
        this.type = type;
        this.format = format;
    }

    private List<String> fields;

    private ExtractFormat format;

    @JsonIgnore
    private ExtractType type;

    private Map<String, String> columnHeaderNames;

    private Filter filter;

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public ExtractFormat getFormat() {
        return format;
    }

    public void setFormat(ExtractFormat format) {
        this.format = format;
    }

    public ExtractType getType() {
        return type;
    }

    public void setType(ExtractType type) {
        this.type = type;
    }

    public Map<String, String> getColumnHeaderNames() {
        return columnHeaderNames;
    }

    public void setColumnHeaderNames(Map<String, String> columnHeaderNames) {
        this.columnHeaderNames = columnHeaderNames;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    // convenience methods
    @JsonIgnore
    public void setCreatedAtFilter(LocalDateTime startAt, LocalDateTime endAt) {
        if (filter == null) {
            filter = new CreateJobRequest.Filter();
        }
        var caf = new CreateJobRequest.Filter.CreatedAtFilter();
        caf.setStartAt(startAt);
        caf.setEndAt(endAt);
        filter.setCreatedAt(caf);
    }

    @JsonIgnore
    public void setActivityIdsFilter(Integer[] ids) {
        if (filter == null) {
            filter = new CreateJobRequest.Filter();
        }
        filter.setActivityTypeIds(ids);
    }

    public static class Filter {
        private CreatedAtFilter createdAt;

        // activities only (not leads)
        private Integer[] activityTypeIds;

        public CreatedAtFilter getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(CreatedAtFilter createdAt) {
            this.createdAt = createdAt;
        }

        public Integer[] getActivityTypeIds() {
            return activityTypeIds;
        }

        public void setActivityTypeIds(Integer[] activityTypeIds) {
            this.activityTypeIds = activityTypeIds;
        }

        public static class CreatedAtFilter {
            private String startAt;
            private String endAt;

            public String getStartAt() {
                return startAt;
            }

            public void setStartAt(LocalDateTime startAt) {
                this.startAt = DATES_FROM_MARKETO_FORMAT.format(startAt);
            }

            public String getEndAt() {
                return endAt;
            }

            public void setEndAt(LocalDateTime endAt) {
                this.endAt = DATES_FROM_MARKETO_FORMAT.format(endAt);
            }
        }

        public static class ActivityIdsFilter {
            private Integer[] ids;

            public Integer[] getIds() {
                return ids;
            }

            public void setIds(Integer[] ids) {
                this.ids = ids;
            }
        }
    }
}
