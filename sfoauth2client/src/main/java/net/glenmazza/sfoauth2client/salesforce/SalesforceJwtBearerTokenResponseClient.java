package net.glenmazza.sfoauth2client.salesforce;

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * Class responsible for making access token request to authorization server using JWT bearer token method.
 * Similar to Spring-created OAuth2AccessTokenResponseClient implementations for password and client grants.
 */
public class SalesforceJwtBearerTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2SalesforceJwtBearerGrantRequest> {
    private static final String INVALID_TOKEN_RESPONSE_ERROR_CODE = "invalid_token_response";

    private Converter<OAuth2SalesforceJwtBearerGrantRequest, RequestEntity<?>> requestEntityConverter =
            new OAuth2SalesforceJwtBearerGrantRequestEntityConverter();


    private RestOperations restOperations;

    public SalesforceJwtBearerTokenResponseClient() {
        RestTemplate restTemplate = new RestTemplate(Arrays.asList(
                new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter()));
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        this.restOperations = restTemplate;
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2SalesforceJwtBearerGrantRequest jwtBearerGrantRequest) {
        Assert.notNull(jwtBearerGrantRequest, "OAuth2SalesforceJwtBearerGrantRequest object cannot be null");

        RequestEntity<?> request = this.requestEntityConverter.convert(jwtBearerGrantRequest);

        ResponseEntity<OAuth2AccessTokenResponse> response;

        try {
            response = this.restOperations.exchange(request, OAuth2AccessTokenResponse.class);
        } catch (RestClientException ex) {
            OAuth2Error oauth2Error = new OAuth2Error(INVALID_TOKEN_RESPONSE_ERROR_CODE,
                    "An error occurred while attempting to retrieve the OAuth 2.0 Access Token Response: " + ex.getMessage(), null);
            throw new OAuth2AuthorizationException(oauth2Error, ex);
        }

        OAuth2AccessTokenResponse tokenResponse = response.getBody();

        if (CollectionUtils.isEmpty(tokenResponse.getAccessToken().getScopes())) {
            // As per spec, in Section 5.1 Successful Access Token Response
            // https://tools.ietf.org/html/rfc6749#section-5.1
            // If AccessTokenResponse.scope is empty, then default to the scope
            // originally requested by the client in the Token Request
            tokenResponse = OAuth2AccessTokenResponse.withResponse(tokenResponse)
                    .scopes(jwtBearerGrantRequest.getClientRegistration().getScopes())
                    .build();
        }

        return tokenResponse;
    }
}
