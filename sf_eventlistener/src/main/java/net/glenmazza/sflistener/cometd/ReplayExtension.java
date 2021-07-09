/*
 * Copyright (c) 2016, salesforce.com, inc.
 * All rights reserved.
 * Licensed under the BSD 3-Clause license.
 * For full license text, see LICENSE.TXT file in the repo root  or https://opensource.org/licenses/BSD-3-Clause
 */
package net.glenmazza.sflistener.cometd;

import org.cometd.bayeux.Channel;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSession;
import org.cometd.bayeux.client.ClientSession.Extension;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * The Bayeux extension for replay
 *
 * @author hal.hildebrand
 * @since 202
 */
public class ReplayExtension implements Extension {
    private static final String EXTENSION_NAME = "replay";
    private static final String EVENT_KEY = "event";
    private static final String REPLAY_ID_KEY = "replayId";

    private final ConcurrentMap<String, Long> dataMap;
    private final AtomicBoolean supported = new AtomicBoolean();

    ReplayExtension(ConcurrentMap<String, Long> dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public boolean rcv(ClientSession session, Message.Mutable message) {
        Long replayId = getReplayId(message);
        if (this.supported.get() && replayId != null) {
            try {
                dataMap.put(message.getChannel(), replayId);
            } catch (ClassCastException e) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean rcvMeta(ClientSession session, Message.Mutable message) {
        if (Channel.META_HANDSHAKE.equals(message.getChannel())) {
            Map<String, Object> ext = message.getExt(false);
            this.supported.set(ext != null && Boolean.TRUE.equals(ext.get(EXTENSION_NAME)));
        }
        return true;
    }

    @Override
    public boolean sendMeta(ClientSession session, Message.Mutable message) {
        switch (message.getChannel()) {
            case Channel.META_HANDSHAKE:
                message.getExt(true).put(EXTENSION_NAME, Boolean.TRUE);
                break;
            case Channel.META_SUBSCRIBE:
                if (supported.get()) {
                    message.getExt(true).put(EXTENSION_NAME, dataMap);
                }
                break;
            default:
        }
        return true;
    }

    private static Long getReplayId(Message.Mutable message) {
        Map<String, Object> data = message.getDataAsMap();
        @SuppressWarnings("unchecked")
        Optional<Long> optional = resolve(() -> (Long) ((Map<String, Object>) data.get(EVENT_KEY)).get(REPLAY_ID_KEY));
        return optional.orElse(null);
    }

    private static <T> Optional<T> resolve(Supplier<T> resolver) {
        try {
            T result = resolver.get();
            return Optional.ofNullable(result);
        } catch (NullPointerException e) {
            return Optional.empty();
        }
    }
}
