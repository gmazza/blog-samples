package net.glenmazza.cdclistener;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "salesforce.cometd")
public class SalesforceConfig {

    private String jwtAudience;

    private String url;

    private String username;

    private String clientId;

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
