package net.glenmazza.sflistener.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import net.glenmazza.sflistener.cometd.EventType;
import net.glenmazza.sflistener.model.AccountAndContactsPlatformEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AccountAndContactsProcessor extends BaseEventProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountAndContactsProcessor.class);

    public AccountAndContactsProcessor(@Value("${salesforce.cometd.accountContactsPEChannel:}") String channel,
                               @Value("${salesforce.cometd.accountContactsPEReplayId:0}") long replayId) {
        this.channel = channel;
        this.replayId = replayId;
    }

    @Override
    public EventType getEventType() {
        return EventType.ACCOUNT_AND_CONTACTS;
    }

    @Override
    public void processEvent(Object eventMap) {
        var event = objectMapper.convertValue(eventMap, AccountAndContactsPlatformEvent.class);
        var payload = event.getPayload();
        
        try {
            if (payload.getContactsString() != null) {
                payload.getContacts().addAll(objectMapper.readValue(payload.getContactsString(), new TypeReference<>() {}));
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Problem parsing contacts for account {} ({}): ", payload.getName(), payload.getId(), e);
        }

        LOGGER.info("Incoming AccountAndContacts message, Account {} ({}) with contacts: {}", payload.getName(), payload.getId(),
                payload.getContacts().stream().map(c -> String.format("%s - %s %s (%s)", c.getUserId(), c.getFirstName(),
                        c.getLastName(), c.getEmailAddress())).collect(Collectors.joining(", ")));
    }
}
