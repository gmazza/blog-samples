package net.glenmazza.sfoauth2client.salesforce;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RemoveAuthorizedClientOAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import static net.glenmazza.sfoauth2client.salesforce.SalesforceJwtBearerOAuth2AuthorizedClientProvider.BEARER_TOKEN_AUD;
import static net.glenmazza.sfoauth2client.salesforce.SalesforceJwtBearerOAuth2AuthorizedClientProvider.BEARER_TOKEN_PRIVATE_KEY_PROP;

@Configuration
@EnableConfigurationProperties({OAuth2ClientProperties.class})
public class SalesforceOAuth2Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(SalesforceOAuth2Config.class);

    @Value("${salesforce.oauth2.resourceowner.username}")
    private String username;

    @Value("${salesforce.oauth2.resourceowner.password:}")
    private String password;

    @Value("${salesforce.oauth2.client.privatekey:}")
    private String bearerTokenPrivateKey;

    @Value("${salesforce.oauth2.jwtbearertoken.audience:}")
    private String bearerTokenAudience;

    @Bean
    public OAuth2AuthorizedClientManager salesforceAuthorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                           OAuth2AuthorizedClientService authorizedClientService,
                                                                           OAuth2AuthorizationFailureHandler failureHandler) {


        OAuth2AuthorizedClientProvider clientProvider;

        Map<String, Object> accessTokenRequestProperties = new HashMap<>();

        // username needed for both grant methods supported
        accessTokenRequestProperties.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);

        if (StringUtils.isNotBlank(password) && StringUtils.isBlank(bearerTokenPrivateKey)) {
            // original inspiration for this flow from https://tinyurl.com/usernamepwdflow
            LOGGER.info("Password grant being used to obtain access tokens");
            accessTokenRequestProperties.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);

            clientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                    .password()
                    .build();

        } else if (StringUtils.isBlank(password) && StringUtils.isNotBlank(bearerTokenPrivateKey) &&
                StringUtils.isNotBlank(bearerTokenAudience)) {
            LOGGER.info("JWT Bearer Token Grant (RFC 7523) being used to obtain access tokens.");

            accessTokenRequestProperties.put(BEARER_TOKEN_AUD, bearerTokenAudience);

            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            byte[] keyBytes = decoder.decode(bearerTokenPrivateKey);
            PrivateKey privateKey;
            try {
                privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
            accessTokenRequestProperties.put(BEARER_TOKEN_PRIVATE_KEY_PROP, privateKey);

            clientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                    .provider(new SalesforceJwtBearerOAuth2AuthorizedClientProvider())
                    .build();

        } else {
            throw new IllegalStateException(" Could not determine access token grant method.  " +
                    "Exactly one of salesforce.oauth2 .resourceowner.password or (.client.privatekey and .jwtbearertoken.audience) " +
                    "must be provided.");
        }

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(clientProvider);
        authorizedClientManager.setContextAttributesMapper(request -> accessTokenRequestProperties);
        authorizedClientManager.setAuthorizationFailureHandler(failureHandler);

        return authorizedClientManager;
    }


    /** Using a single failure handler between the AuthorizedClientManager (which adds client access
     *  tokens) and the ServletOAuth2AuthorizedClientExchangeFilterFunction (which
     */
    @Bean
    public OAuth2AuthorizationFailureHandler authorizationFailureHandler(OAuth2AuthorizedClientService authorizedClientService) {
        return new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) ->
                        authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName()));
    }

    @Bean
    WebClient webClient(@Qualifier("salesforceAuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
                        OAuth2AuthorizationFailureHandler failureHandler) {
        // Uncomment // lines & set logging as given in application.properties to activate debug logging of requests & responses
        // (https://www.baeldung.com/spring-log-webclient-calls#logging-request-repsonse)
        //HttpClient httpClient = HttpClient.create().wiretap(true);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oAuth2Filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oAuth2Filter.setDefaultClientRegistrationId("mysalesforce");
        oAuth2Filter.setAuthorizationFailureHandler(failureHandler);

        return WebClient.builder()
                .filter(oAuth2Filter)
                // if logging (see above)
                //.clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
