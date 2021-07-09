package net.glenmazza.sflistener.cometd;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.glenmazza.sflistener.service.CometDService;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LoggingListener implements ClientSessionChannel.MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingListener.class);
    private CometDService cometDService;

    private boolean logSuccess;
    private boolean logFailure;

    public LoggingListener(CometDService cometDService) {
        this.cometDService = cometDService;
        this.logSuccess = true;
        this.logFailure = true;
    }

    public LoggingListener(CometDService cometDService, boolean logSuccess, boolean logFailure) {
        this.cometDService = cometDService;
        this.logSuccess = logSuccess;
        this.logFailure = logFailure;
    }

    @Override
    public void onMessage(ClientSessionChannel clientSessionChannel, Message message) {
        writeLog(clientSessionChannel, message);
    }

    protected void writeLog(ClientSessionChannel clientSessionChannel, Message message) {
        StringBuilder logMessage = new StringBuilder();
        if (logSuccess && message.isSuccessful()) {
            logMessage.append(">>>>");
            logMessage.append(getPrefix());
            logMessage.append("Success:[" + clientSessionChannel.getId() + "]");
            logMessage.append("Message: " + message);
            logMessage.append("<<<<");
            LOGGER.info(logMessage.toString());
        }

        if (logFailure && !message.isSuccessful()) {
            logMessage.append(">>>>");
            logMessage.append(getPrefix());
            logMessage.append("Failure:[" + clientSessionChannel.getId() + "]");
            logMessage.append("Message: " + message);
            logMessage.append("<<<<");
            LOGGER.error(logMessage.toString());
        }
    }

    private String getPrefix() {
        return "[" + timeNow() + "] ";
    }

    private String timeNow() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date now = new Date();
        return dateFormat.format(now);
    }

    /**
     * @return the cometdClient
     */
    public CometDService getCometDService() {
        return cometDService;
    }
}
