package net.glenmazza.sfoauth2client.salesforce;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.security.PrivateKey;

/**
 *  Class that checks whether an incoming request for resources has the credentials necessary to
 *  make an access token request using the JWT bearer token grant type, and if so makes that request
 *  if there is not already an authorized client for this grant type with a valid access token.
 */
public class SalesforceJwtBearerOAuth2AuthorizedClientProvider implements OAuth2AuthorizedClientProvider {
    private OAuth2AccessTokenResponseClient<OAuth2SalesforceJwtBearerGrantRequest> accessTokenResponseClient =
            new SalesforceJwtBearerTokenResponseClient();

    public static final String BEARER_TOKEN_PRIVATE_KEY_PROP = "bearerTokenPrivateKey";
    public static final String BEARER_TOKEN_AUD = "bearerTokenAudience";

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {

        ClientRegistration clientRegistration = context.getClientRegistration();
        OAuth2AuthorizedClient authorizedClient = context.getAuthorizedClient();

        // if not bearer grant type, return null for other OAuth2AuthorizedClientProviders to potentially process
        if (!OAuth2SalesforceJwtBearerGrantRequest.BEARER_GRANT_TYPE.equals(clientRegistration.getAuthorizationGrantType().getValue())) {
            return null;
        }

        /*
         If there is already an authorizedClient (i.e., having a valid access token)
         just return as that client can be used to make the call.
        */
        if (authorizedClient != null) {
            return null;
        }

        /*
         During a failed call due to an expired access token, code in SalesforceOAuth2Config
         deletes the authorizedClient, making it null.  For a subsequent call, below code
         will then be activated to obtain another authorizedClient.

         In SalesforceService, the webclient call has a retry(1) so a 2nd attempt will be
         automatically made in case of an expired token (or other exception), allowing
         for a new access token and successful call.
        */

        String username = context.getAttribute(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME);
        PrivateKey privateKey = context.getAttribute(BEARER_TOKEN_PRIVATE_KEY_PROP);
        String audience = context.getAttribute(BEARER_TOKEN_AUD);

        // if we don't have the information necessary for bearer token method, try another client
        if (StringUtils.isBlank(username) || privateKey == null || audience == null) {
            return null;
        }

        // create holder of info necessary for bearer token request
        OAuth2SalesforceJwtBearerGrantRequest jwtAccessTokenRequest =
                new OAuth2SalesforceJwtBearerGrantRequest(clientRegistration, username, audience, privateKey);

        OAuth2AccessTokenResponse tokenResponse;
        try {
            tokenResponse = this.accessTokenResponseClient.getTokenResponse(jwtAccessTokenRequest);
        } catch (OAuth2AuthorizationException ex) {
            throw new ClientAuthorizationException(ex.getError(), clientRegistration.getRegistrationId(), ex);
        }

        return new OAuth2AuthorizedClient(clientRegistration, context.getPrincipal().getName(),
                tokenResponse.getAccessToken());
    }
}
