package net.glenmazza.sflistener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.glenmazza.sflistener.cometd.EventType;

public abstract class BaseEventProcessor {

    ObjectMapper objectMapper;
    String channel;
    long replayId;

    public String getChannel() {
        return channel;
    }

    public long getStartupReplayId() {
        return replayId;
    }

    public abstract EventType getEventType();

    public abstract void processEvent(Object eventMap);

    public BaseEventProcessor() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                // allow "Name" in JSON to map to "name" in class
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                // timestamps to Instant (https://stackoverflow.com/q/45762857/1207540)
                .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

    String getJson(Object eventMap) {
        try {
            return objectMapper.writeValueAsString(eventMap);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
