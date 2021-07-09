package net.glenmazza.sflistener.cometd;

import java.util.concurrent.TimeUnit;

import net.glenmazza.sflistener.service.CometDService;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectListener extends LoggingListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectListener.class);
    private static final long MAX_RECONNECT_ATTEMPTS = 10;
    private static final long INIT_RECONNECT_DELAY_MS = 500;
    private int reconnectAttempts = 0;
    private long lastReconnectDelay = INIT_RECONNECT_DELAY_MS;

    public ConnectListener(CometDService client) {
        super(client);
    }

    @Override
    public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {
        super.onMessage(clientSessionChannel, message);
        if (message.isSuccessful()) {
            reconnectAttempts = 0;
            lastReconnectDelay = INIT_RECONNECT_DELAY_MS;
        } else {
            if (++reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                lastReconnectDelay *= 2;
                final long sleepTime = lastReconnectDelay;
                LOGGER.info("Connection attempt #{} failed, trying to reconnect in {} sec", reconnectAttempts,
                        Math.round(lastReconnectDelay / 1000));
                new Thread(() -> {
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleepTime);
                        LOGGER.info("Re-connecting cometd client...");
                        getCometDService().reconnect();
                    } catch (InterruptedException e) {
                        LOGGER.warn("Failed to re-connect cometd client: interrupted", e);
                    }
                });
            } else {
                LOGGER.error("Failed to connect cometd client {} times; giving up", reconnectAttempts);
            }
        }
    }
}
