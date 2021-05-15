package net.glenmazza.cdclistener.processor;

import net.glenmazza.cdclistener.cometd.EventType;
import net.glenmazza.cdclistener.model.AccountCDCEvent;
import net.glenmazza.cdclistener.model.AccountCDCEvent.Payload;
import net.glenmazza.cdclistener.model.BaseCDCEvent.BasePayload.ChangeEventHeader.ChangeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

import static net.glenmazza.cdclistener.model.BaseCDCEvent.*;
import static net.glenmazza.cdclistener.model.BaseCDCEvent.BasePayload.ChangeEventHeader.ChangeType.UNDELETE;

@Component
public class AccountCDCProcessor extends BaseEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCDCProcessor.class);

    public AccountCDCProcessor(@Value("${salesforce.cometd.accountCdcChannel:}") String channel,
                               @Value("${salesforce.cometd.accountCdcReplayId:}") long replayId) {
        this.channel = channel;
        this.replayId = replayId;
    }

    @Override
    public EventType getEventType() {
        return EventType.ACCOUNT;
    }

    @Override
    public void processEvent(Object eventMap) {
        AccountCDCEvent event = objectMapper.convertValue(eventMap, AccountCDCEvent.class);

        Payload payload = event.getPayload();
        long replayId = event.getEvent().getReplayId();
        BasePayload.ChangeEventHeader ceh = payload.getChangeEventHeader();

        ChangeType ct = ceh.getChangeType();
        switch (ct) {
            case CREATE:
            case UNDELETE:
                addAccount(payload, ct);
                break;
            case UPDATE:
                updateAccount(payload);
                break;
            case DELETE:
                deleteAccount(payload);
                break;
            case GAP_OVERFLOW:
                String entity = ceh.getEntityName();
                reportGapOverflow(replayId, entity);
                break;
            case GAP_CREATE:
            case GAP_DELETE:
            case GAP_UPDATE:
            case GAP_UNDELETE:
                reportGapEvent(ct, replayId, ceh);
                break;
            default:
                LOGGER.error("Not yet supported change type {} for replay id {}", ct, replayId);

        }
    }

    // ordinarily DB actions would be done in below handlers.
    // be aware of potential Gap events and how you might wish to handle them (presently logged in parent class).

    private void addAccount(Payload payload, ChangeType ct) {
        String recordId = payload.getChangeEventHeader().getRecordIds().get(0);
        LOGGER.info("Account {}: {}, employee count {}, rating {}",
                UNDELETE.equals(ct) ? "undeleted" : "added", descAccount(payload.getName(), recordId),
                payload.getNumberOfEmployees(), payload.getRating());
    }

    private void deleteAccount(Payload payload) {
        for (String recordId : payload.getChangeEventHeader().getRecordIds()) {
            LOGGER.info("Account deleted: {}", descAccount(payload.getName(), recordId));
        }
    }

    private void updateAccount(Payload payload) {
        List<String> changedFields = payload.getChangeEventHeader().getChangedFields();

        for (String recordId : payload.getChangeEventHeader().getRecordIds()) {
            StringBuilder recordUpdate = new StringBuilder(
                    String.format("Account %s updated: ", descAccount(payload.getName(), recordId)));

            for (String field : changedFields) {
                switch (field) {
                    case "Name":
                        recordUpdate.append(String.format("Name to %s ", payload.getName()));
                        break;
                    case "NumberOfEmployees":
                        recordUpdate.append(String.format("Num employees to %s ", payload.getNumberOfEmployees()));
                        break;
                    case "Rating":
                        recordUpdate.append(String.format("Rating to %s ", payload.getRating()));
                        break;
                }
            }
            LOGGER.info(recordUpdate.toString());
        }
    }

    // if Account Name is added to CDC messages (if it is an enriched field), add it in.
    // note if enriched, then will no longer get multiple records per event due to each
    // record having different enriched field values.
    private static String descAccount(String name, String recordId) {
        return (name != null) ? String.format("%s (%s)", name, recordId) : recordId;
    }

}
