package net.glenmazza.sfclient.oauth2;

import org.springframework.security.oauth2.client.endpoint.AbstractOAuth2AuthorizationGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.util.Assert;

import java.security.PrivateKey;

/**
 * Serves a holder for the information needed to obtain a Salesforce access token using a JWT Bearer token.
 */
public class OAuth2SalesforceJwtBearerGrantRequest extends AbstractOAuth2AuthorizationGrantRequest {
    private final ClientRegistration clientRegistration;
    private final String username;
    private final String audience;
    private final PrivateKey privateKey;

    public static final String BEARER_GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    public OAuth2SalesforceJwtBearerGrantRequest(ClientRegistration clientRegistration, String username,
                                                 String audience, PrivateKey privateKey) {
        super(new AuthorizationGrantType(BEARER_GRANT_TYPE));
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        Assert.notNull(privateKey, "private key for signing JWT cannot be null");
        Assert.isTrue(BEARER_GRANT_TYPE.equals(clientRegistration.getAuthorizationGrantType().getValue()),
                "clientRegistration.authorizationGrantType must be " + BEARER_GRANT_TYPE);
        Assert.hasText(username, "username cannot be empty");

        this.clientRegistration = clientRegistration;
        this.username = username;
        this.audience = audience;
        this.privateKey = privateKey;
    }

    public ClientRegistration getClientRegistration() {
        return clientRegistration;
    }

    public String getUsername() {
        return username;
    }

    public String getAudience() {
        return audience;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

}
