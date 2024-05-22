package net.glenmazza.marketoclient.marketoclient.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.glenmazza.marketoclient.marketoclient.model.AbstractMarketoResponse;
import net.glenmazza.marketoclient.marketoclient.util.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.ClientAuthorizationException;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.web.reactive.function.client.WebClient;

public abstract class AbstractRESTService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRESTService.class);

    protected OAuth2AuthorizationFailureHandler authorizationFailureHandler;

    @Value("${marketo.api.base-url}")
    protected String baseUrl;

    protected WebClient webClient;

    protected final ObjectMapper objectMapper;

    protected AbstractRESTService(WebClient webClient, OAuth2AuthorizationFailureHandler authorizationFailureHandler) {
        this.objectMapper = JSONUtils.createObjectMapper();
        this.webClient = webClient;
        this.authorizationFailureHandler = authorizationFailureHandler;
    }

    private static final Authentication ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken("anonymous",
            "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

    protected void checkForMarketoErrorResponses(AbstractMarketoResponse response) {
        checkAccessTokenExpiredError(response);
        checkTooFrequentRequestsError(response);
    }

    private void checkAccessTokenExpiredError(AbstractMarketoResponse response) {
        if (!response.isSuccess() && response.getErrors() != null && "602".equals(response.getErrors().get(0).getCode())) {
            LOGGER.info("On call attempt, Marketo indicating 602/Access token expired, deleting access token and retrying...");

            OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN, null,
                    "https://tools.ietf.org/html/rfc6750#section-3.1");

            ClientAuthorizationException authorizationException = new ClientAuthorizationException(oauth2Error,
                    "marketoclient");

            // causes the access token to get deleted, so a new one will be requested for the next call
            authorizationFailureHandler.onAuthorizationFailure(authorizationException,
                    ANONYMOUS_AUTHENTICATION,
                    null);
            throw new MarketoAccessTokenExpiredException();
        }
    }

    private void checkTooFrequentRequestsError(AbstractMarketoResponse response) {
        if (response.tooManyRequestsError()) {
            LOGGER.info("On call attempt, 606/too frequent requests returned from Marketo, pausing and trying again...");
            throw new MarketoTooFrequentRequestsException();
        }
    }

    public static class MarketoAccessTokenExpiredException extends RuntimeException {
    }

    public static class MarketoTooFrequentRequestsException extends RuntimeException {
    }

    public void setWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

}
