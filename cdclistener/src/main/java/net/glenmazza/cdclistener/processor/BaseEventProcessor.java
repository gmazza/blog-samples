package net.glenmazza.cdclistener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.glenmazza.cdclistener.cometd.EventType;
import net.glenmazza.cdclistener.model.BaseCDCEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See here for Salesforce guide on Change Data Capture:
 * https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_intro.htm
 * https://trailhead.salesforce.com/en/content/learn/trails/design-eventdriven-apps-for-realtime-integration
 */
public abstract class BaseEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEventProcessor.class);

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

    // these handlers generic for any entity, but can be overridden as helpful.
    // overflow events: https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_other_events_overflow.htm
    // gap events: https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_other_events_gap.htm
    void reportGapEvent(BaseCDCEvent.BasePayload.ChangeEventHeader.ChangeType ct, long replayId, BaseCDCEvent.BasePayload.ChangeEventHeader ceh) {
        String entity = ceh.getEntityName();
        String recordIds = String.join(", ", ceh.getRecordIds());
        LOGGER.warn("Replay ID {}, Salesforce unable to provide events on entity {} and change type {} for records {}",
                replayId, entity, ct, recordIds);
    }

    void reportGapOverflow(long replayId, String entity) {
        LOGGER.warn("Replay ID {}, too many changes for entity {}, only first 100K records received events", replayId,
                entity);
    }
}
