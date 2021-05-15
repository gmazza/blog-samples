/*
 * Copyright (c) 2016, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.TXT file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package net.glenmazza.cdclistener.cometd;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.client.BayeuxClient;
import org.cometd.client.http.jetty.JettyHttpClientTransport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.cometd.client.transport.ClientTransport.MAX_MESSAGE_SIZE_OPTION;
import static org.cometd.client.transport.ClientTransport.MAX_NETWORK_DELAY_OPTION;

/**
 * Modified from original version by hal.hildebrand
 * in EMP-Connector Github project.
 */
public class EmpConnector {
    private static final String ERROR = "error";
    private static final String FAILURE = "failure";

    private static final Logger LOGGER = LoggerFactory.getLogger(EmpConnector.class);

    public final class SubscriptionImpl implements TopicSubscription {
        private final String topic;
        private final Consumer<Object> consumer;

        private SubscriptionImpl(String topic, Consumer<Object> consumer) {
            this.topic = topic;
            this.consumer = consumer;
            subscriptions.add(this);
        }

        @Override
        public void cancel() {
            replay.remove(topic);
            if (running.get() && client != null) {
                client.getChannel(topic).unsubscribe();
                subscriptions.remove(this);
            }
        }

        @Override
        public long getReplayFrom() {
            return replay.getOrDefault(topic, REPLAY_FROM_EARLIEST);
        }

        @Override
        public String getTopic() {
            return topic;
        }

        @Override
        public String toString() {
            return String.format("Subscription [%s with replayId %s]", getTopic(), getReplayFrom());
        }

        Future<TopicSubscription> subscribe() {
            long replayFrom = getReplayFrom();
            ClientSessionChannel channel = client.getChannel(this.topic);
            CompletableFuture<TopicSubscription> future = new CompletableFuture<>();
            channel.subscribe(new MessageAsObjectListener(consumer), message -> {
                if (message.isSuccessful()) {
                    future.complete(this);
                } else {
                    Object error = message.get(ERROR);
                    if (error == null) {
                        error = message.get(FAILURE);
                    }
                    future.completeExceptionally(new CannotSubscribeException(cometDEndpoint, this.topic, replayFrom,
                            error != null ? error : message));
                }
            });
            return future;
        }
    }

    public static class MessageAsObjectListener implements ClientSessionChannel.MessageListener {

        private Consumer<Object> consumer;

        MessageAsObjectListener(Consumer<Object> consumer) {
            this.consumer = consumer;
        }

        @Override
        public void onMessage(ClientSessionChannel channel, Message message) {
            consumer.accept(message.getData());
        }
    }

    // -1L is from top of stack (i.e., all new messages)
    private static final long REPLAY_FROM_EARLIEST = -2L;

    private static final String AUTHORIZATION = "Authorization";
    private static final Logger LOG = LoggerFactory.getLogger(EmpConnector.class);

    private volatile BayeuxClient client;
    private final HttpClient httpClient;
    private final URL cometDEndpoint;
    private final ConcurrentMap<String, Long> replay = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean();

    private final Set<SubscriptionImpl> subscriptions = new CopyOnWriteArraySet<>();
    private final Set<MessageListenerInfo> listenerInfos = new CopyOnWriteArraySet<>();

    private Function<Boolean, String> accessTokenProvider;
    private final AtomicBoolean reauthenticate = new AtomicBoolean(true);

    public EmpConnector(String url) {
        httpClient = new HttpClient(new SslContextFactory());

        try {
            cometDEndpoint = new URL(url + "/cometd/51.0");
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(String.format("Unable to create url from instance_url %s",
                    url), e);
        }
    }

    /**
     * Start the connector.
     *
     * @return true if connection was established, false otherwise
     */
    public Future<Boolean> start() {
        if (running.compareAndSet(false, true)) {
            addListenerToChannels(new AuthFailureListener(), Set.of(Channel.META_CONNECT, Channel.META_HANDSHAKE));
            return connect();
        }
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(true);
        return future;
    }

    /**
     * Stop the connector
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return;
        }
        if (client != null) {
            client.disconnect();
            client = null;
        }
        if (httpClient != null) {
            try {
                httpClient.stop();
            } catch (Exception e) {
                LOG.error("Unable to stop HTTP transport[{}]", cometDEndpoint, e);
            }
        }
    }

    /**
     * Set a bearer token / session id provider function that takes a boolean as
     * input and returns a valid token. If the input is true, the provider function
     * is supposed to re-authenticate with the Salesforce server and get a fresh
     * session id or token.
     *
     * @param bearerTokenProvider a bearer token provider function.
     */
    public void setAccessTokenProvider(Function<Boolean, String> bearerTokenProvider) {
        this.accessTokenProvider = bearerTokenProvider;
    }

    /**
     * Subscribe to a topic, receiving events after the replayFrom position
     *
     * @param topic      - the topic to subscribe to
     * @param replayFrom - the replayFrom position in the event stream
     * @param consumer   - the consumer of the events
     * @return a Future returning the Subscription - on completion returns a
     *         Subscription or throws a CannotSubscribeException exception
     */
    public Future<TopicSubscription> subscribe(String topic, long replayFrom,
                                               Consumer<Object> consumer) {
        if (!running.get()) {
            throw new IllegalStateException(String.format("Connector[%s} has not been started", cometDEndpoint));
        }

        Long oldValue = replay.putIfAbsent(topic, replayFrom);
        if (oldValue != null) {
            LOGGER.warn("Retaining current replay value of {} for topic {}, ignoring new value {}", oldValue, topic,
                    replayFrom);
        }

        SubscriptionImpl subscription = new SubscriptionImpl(topic, consumer);

        return subscription.subscribe();
    }

    public void addListenerToChannels(ClientSessionChannel.MessageListener messageListener, Set<String> channels) {
        for (String channel : channels) {
            listenerInfos.add(new MessageListenerInfo(channel, messageListener));
        }
    }

    private static Map<String, Object> longPollingOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put(MAX_NETWORK_DELAY_OPTION, 15000);
        options.put("maxBufferSize", FileUtils.ONE_MB);
        options.put(MAX_MESSAGE_SIZE_OPTION, 10 * FileUtils.ONE_MB);
        return options;
    }

    private Future<Boolean> connect() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        replay.clear();
        try {
            httpClient.start();
        } catch (Exception e) {
            LOG.error("Unable to start HTTP transport[{}]", cometDEndpoint, e);
            running.set(false);
            future.complete(false);
            return future;
        }

        String accessToken = accessToken();

        JettyHttpClientTransport httpTransport = new JettyHttpClientTransport(longPollingOptions(), httpClient) {
            @Override
            protected void customize(Request request) {
                request.header(AUTHORIZATION, accessToken);
            }
        };

        client = new BayeuxClient(cometDEndpoint.toExternalForm(), httpTransport);

        client.addExtension(new ReplayExtension(replay));

        addListeners(client);

        client.handshake((m) -> {
            if (!m.isSuccessful()) {
                Object error = m.get(ERROR);
                if (error == null) {
                    error = m.get(FAILURE);
                }
                future.completeExceptionally(new ConnectException(String.format("Cannot connect [%s] : %s",
                        cometDEndpoint, error)));
                running.set(false);
            } else {
                subscriptions.forEach(SubscriptionImpl::subscribe);
                future.complete(true);
            }
        });

        return future;
    }

    private void addListeners(BayeuxClient client) {
        for (MessageListenerInfo info : listenerInfos) {
            client.getChannel(info.getChannelName()).addListener(info.getMessageListener());
        }
    }

    private String accessToken() {
        String accessToken = accessTokenProvider.apply(reauthenticate.get());
        reauthenticate.compareAndSet(true, false);
        return accessToken;
    }

    /**
     * Listens to /meta/connect topic messages and handles 401 errors, where
     * client needs to reauthenticate.
     */
    private class AuthFailureListener implements ClientSessionChannel.MessageListener {

        @Override
        public void onMessage(ClientSessionChannel channel, Message message) {
            if (!message.isSuccessful()) {
                if (is401Error(message)) {
                    reauthenticate.set(true);
                    stop();
                    connect();
                }
            }
        }

        private boolean is401Error(Message message) {
            String error = (String) message.get(Message.ERROR_FIELD);
            String failureReason = getFailureReason(message);

            return (error != null && error.startsWith("401")) || (failureReason != null && failureReason
                    .startsWith("401"));
        }

        private String getFailureReason(Message message) {
            String failureReason = null;
            Map<String, Object> ext = message.getExt();
            if (ext != null) {
                Map<String, Object> sfdc = (Map<String, Object>) ext.get("sfdc");
                if (sfdc != null) {
                    failureReason = (String) sfdc.get("failureReason");
                }
            }
            return failureReason;
        }
    }

    private static class MessageListenerInfo {
        private String channelName;
        private ClientSessionChannel.MessageListener messageListener;

        MessageListenerInfo(String channelName, ClientSessionChannel.MessageListener messageListener) {
            this.channelName = channelName;
            this.messageListener = messageListener;
        }

        String getChannelName() {
            return channelName;
        }

        ClientSessionChannel.MessageListener getMessageListener() {
            return messageListener;
        }
    }
}
