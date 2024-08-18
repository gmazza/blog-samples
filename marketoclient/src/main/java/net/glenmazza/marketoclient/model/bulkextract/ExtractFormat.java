package net.glenmazza.marketoclient.model.bulkextract;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ExtractFormat {
    CSV("CSV");

    private final String formatText;

    ExtractFormat(String formatText) {
        this.formatText = formatText;
    }

    @JsonValue
    public String getFormatText() {
        return formatText;
    }
}
