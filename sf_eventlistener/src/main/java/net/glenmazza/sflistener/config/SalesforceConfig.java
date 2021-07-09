package net.glenmazza.sflistener;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Property values mostly ignore the default salesforce.cometd prefix.
 * Instead, using the property names used by the Salesforce API Client tutorial
 * to facilitate projects using both an Event Listener and API support.
 */
@ConfigurationProperties(prefix = "salesforce.cometd")
public class SalesforceConfig {

    @Value("${salesforce.oauth2.jwtbearertoken.audience}")
    private String jwtAudience;

    @Value("${salesforce.api.base-url}")
    private String url;

    @Value("${salesforce.oauth2.resourceowner.username}")
    private String username;

    @Value("${spring.security.oauth2.client.registration.sfclient.client-id}")
    private String clientId;

    @Value("${salesforce.oauth2.client.privatekey}")
    private String privateKeyForConnectedApp;

    private String accountCdcChannel;

    private int accountCdcReplayId;

    private int connectorTimeoutSecs = 10;

    public String getJwtAudience() {
        return jwtAudience;
    }

    public void setJwtAudience(String jwtAudience) {
        this.jwtAudience = jwtAudience;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPrivateKeyForConnectedApp() {
        return privateKeyForConnectedApp;
    }

    public void setPrivateKeyForConnectedApp(String privateKeyForConnectedApp) {
        this.privateKeyForConnectedApp = privateKeyForConnectedApp;
    }

    public String getAccountCdcChannel() {
        return accountCdcChannel;
    }

    public void setAccountCdcChannel(String accountCdcChannel) {
        this.accountCdcChannel = accountCdcChannel;
    }

    public int getAccountCdcReplayId() {
        return accountCdcReplayId;
    }

    public void setAccountCdcReplayId(int accountCdcReplayId) {
        this.accountCdcReplayId = accountCdcReplayId;
    }

    public int getConnectorTimeoutSecs() {
        return connectorTimeoutSecs;
    }

    public void setConnectorTimeoutSecs(int connectorTimeoutSecs) {
        this.connectorTimeoutSecs = connectorTimeoutSecs;
    }
}
