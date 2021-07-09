package net.glenmazza.sflistener.model;

/**
 * Fields common to all platform events, including Change Data Capture (CDC) events.
 * Subclasses are expected to implement a message-specific payload element.
 */
public class BasePlatformEvent {

    private String schema;

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    private Event event;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public static class Event {
        long replayId;

        public long getReplayId() {
            return replayId;
        }

        public void setReplayId(long replayId) {
            this.replayId = replayId;
        }
    }
}
