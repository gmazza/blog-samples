package net.glenmazza.marketoclient.model.bulkextract;

import com.fasterxml.jackson.annotation.JsonValue;

import java.time.OffsetDateTime;

/**
 * <a href="https://developers.marketo.com/rest-api/bulk-extract/#retrieving_jobs">Marketo Docs</a>
 */
public class Job {

    private String exportId;
    private Status status;
    private OffsetDateTime createdAt;
    private OffsetDateTime queuedAt;
    private OffsetDateTime startedAt;
    private OffsetDateTime finishedAt;

    private int numberOfRecords;

    private int fileSize;

    private String fileChecksum;
    private ExtractFormat format;

    public String getExportId() {
        return exportId;
    }

    public void setExportId(String exportId) {
        this.exportId = exportId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getQueuedAt() {
        return queuedAt;
    }

    public void setQueuedAt(OffsetDateTime queuedAt) {
        this.queuedAt = queuedAt;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public ExtractFormat getFormat() {
        return format;
    }

    public void setFormat(ExtractFormat format) {
        this.format = format;
    }

    public int getNumberOfRecords() {
        return numberOfRecords;
    }

    public void setNumberOfRecords(int numberOfRecords) {
        this.numberOfRecords = numberOfRecords;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileChecksum() {
        return fileChecksum;
    }

    public void setFileChecksum(String fileChecksum) {
        this.fileChecksum = fileChecksum;
    }

    public enum Status {
        CREATED("Created"),
        QUEUED("Queued"),
        PROCESSING("Processing"),
        CANCELLED("Cancelled"),
        COMPLETED("Completed"),
        FAILED("Failed");

        private final String apiValue;

        Status(String apiValue) {
            this.apiValue = apiValue;
        }

        @JsonValue
        public String getApiValue() {
            return apiValue;
        }
    }
}
