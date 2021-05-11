package net.glenmazza.cdclistener.service;

import net.glenmazza.cdclistener.SalesforceConfig;
import net.glenmazza.cdclistener.cometd.AccessTokenProvider;
import net.glenmazza.cdclistener.cometd.CannotSubscribeException;
import net.glenmazza.cdclistener.cometd.ConnectListener;
import net.glenmazza.cdclistener.cometd.EmpConnector;
import net.glenmazza.cdclistener.cometd.LoggingListener;
import net.glenmazza.cdclistener.cometd.TopicSubscription;
import net.glenmazza.cdclistener.processor.BaseEventProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static org.cometd.bayeux.Channel.META_CONNECT;
import static org.cometd.bayeux.Channel.META_DISCONNECT;
import static org.cometd.bayeux.Channel.META_HANDSHAKE;
import static org.cometd.bayeux.Channel.META_SUBSCRIBE;
import static org.cometd.bayeux.Channel.META_UNSUBSCRIBE;

/**
 * All BaseEventProcessors defined in the project having a non-blank channel will be
 * picked up by this service and run.
 */
@Component
@EnableConfigurationProperties(SalesforceConfig.class)
public class CometDService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CometDService.class);
    private static final List<TopicSubscription> SUBSCRIPTIONS = new ArrayList<>();

    private EmpConnector connector;
    private final Set<BaseEventProcessor> processors;
    private final SalesforceConfig sfConfig;

    @Autowired
    public CometDService(SalesforceConfig sfConfig, Set<BaseEventProcessor> processors) {
        this.sfConfig = sfConfig;
        this.processors = processors;
    }

    @PostConstruct
    public void postConstruct() {
        try {
            LOGGER.info("Initializing CometD client...");
            AccessTokenProvider tokenProvider = new AccessTokenProvider(sfConfig.getPrivateKeyForConnectedApp(),
                    sfConfig.getUrl(), sfConfig.getUsername(),
                    sfConfig.getJwtAudience(), sfConfig.getClientId());

            connector = new EmpConnector(sfConfig.getUrl());
            connector.setAccessTokenProvider(tokenProvider);

            // channel listeners
            connector.addListenerToChannels(new LoggingListener(this), Set.of(META_HANDSHAKE,
                    META_DISCONNECT, META_SUBSCRIBE, META_UNSUBSCRIBE));
            connector.addListenerToChannels(new ConnectListener(this), Set.of(META_CONNECT));

            boolean isConnected = connector.start().get(sfConfig.getConnectorTimeoutSecs(), TimeUnit.SECONDS);
            LOGGER.info("Connector started? " + isConnected);
            activateProcessors();
            LOGGER.info("CometD client initialization completed");
        } catch (Exception e) {
            LOGGER.error("Failed to connect to CometD", e);
        }
    }

    private void activateProcessors() {
        processors.stream().filter(p -> StringUtils.isNotBlank(p.getChannel())).forEach(p -> {
            try {
                if (SUBSCRIPTIONS.stream().anyMatch(sub -> sub.getTopic().equals(p.getChannel()))) {
                    // for safety, should never happen (EmpConnector not set up to have multiple subs to same channel)
                    LOGGER.warn("Skipping subscription to {}, it already has a subscriber.", p.getChannel());
                } else {
                    addConsumer(p.getChannel(), p::processEvent, p.getStartupReplayId());
                }
            } catch (Exception e) {
                LOGGER.error("Failed to add consumer for {}", p.getEventType(), e);
            }
        });
    }

    private void addConsumer(String channel, Consumer<Object> consumer, long replayId)
            throws InterruptedException, TimeoutException, ExecutionException {
        TopicSubscription subscription = null;
        if (replayId > 0) {
            try {
                LOGGER.info("Subscribing to {} with specific replayId {}", channel, replayId);
                subscription = connector.subscribe(channel, replayId, consumer).get(sfConfig.getConnectorTimeoutSecs(),
                        TimeUnit.SECONDS);
            } catch (Exception e) {
                if (e.getCause() instanceof CannotSubscribeException && e.getMessage().contains("Please provide a valid ID")) {
                    LOGGER.warn("Failed to connect to channel {} with replay id {}, subscribing from earliest instead",
                            channel, replayId, e);
                    replayId = -2;
                } else {
                    throw e;
                }
            }
        }
        // not else if..., as above code can set replayId to -2 (replay from earliest)
        if (replayId < 0) {
            subscription = connector.subscribe(channel, replayId, consumer).get(sfConfig.getConnectorTimeoutSecs() * 5L,
                    TimeUnit.SECONDS);
        }

        if (subscription != null) {
            SUBSCRIPTIONS.add(subscription);
            LOGGER.info("Added connection to {} starting at replay ID {}", subscription.getTopic(),
                    subscription.getReplayFrom());
        } else {
            LOGGER.warn("Unable to subscribe to channel {} (check logging of CometDService?)", channel);
        }
    }

    public void reconnect() {
        stopConnector();
        connector.start();
        activateProcessors();
    }

    @PreDestroy
    private void stopConnector() {
        LOGGER.info(String.format("Cancelling all subscriptions: %s", SUBSCRIPTIONS));
        SUBSCRIPTIONS.forEach(TopicSubscription::cancel);
        SUBSCRIPTIONS.clear();
        LOGGER.info("Stopping CometD connection");
        connector.stop();
    }

}
