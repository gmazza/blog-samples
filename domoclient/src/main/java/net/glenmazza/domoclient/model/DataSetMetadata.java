package net.glenmazza.domoclient.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSetMetadata {

    String id;
    String name;
    String description;
    Owner owner;
    int rows;
    int columns;
    DataSetSchema schema;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDateTime dataCurrentAt;

    // Personalized Data Permissions (https://domo-support.domo.com/s/article/360042934614?language=en_US)
    boolean pdpEnabled;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public DataSetSchema getSchema() {
        return schema;
    }

    public void setSchema(DataSetSchema schema) {
        this.schema = schema;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDataCurrentAt() {
        return dataCurrentAt;
    }

    public void setDataCurrentAt(LocalDateTime dataCurrentAt) {
        this.dataCurrentAt = dataCurrentAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPdpEnabled() {
        return pdpEnabled;
    }

    public void setPdpEnabled(boolean pdpEnabled) {
        this.pdpEnabled = pdpEnabled;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public static class Owner {
        private String id;
        private String name;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
