package net.glenmazza.sflistener.processor;

import net.glenmazza.sflistener.model.BaseCDCEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See here for Salesforce guide on Change Data Capture:
 * https://developer.salesforce.com/docs/atlas.en-us.change_data_capture.meta/change_data_capture/cdc_intro.htm
 * https://trailhead.salesforce.com/en/content/learn/trails/design-eventdriven-apps-for-realtime-integration

 */
public abstract class BaseCDCProcessor extends BaseEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseCDCProcessor.class);

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
