package net.glenmazza.sfclient.oauth2;

import io.netty.channel.ChannelOption;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
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
import reactor.netty.http.client.HttpClient;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static net.glenmazza.sfclient.oauth2.SalesforceJwtBearerOAuth2AuthorizedClientProvider.BEARER_TOKEN_AUD;
import static net.glenmazza.sfclient.oauth2.SalesforceJwtBearerOAuth2AuthorizedClientProvider.BEARER_TOKEN_PRIVATE_KEY_PROP;

@Configuration
@EnableConfigurationProperties({OAuth2ClientProperties.class})
@ConditionalOnProperty(name = "salesforce.client.enabled", matchIfMissing = true)
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

    @Value("${salesforce.connection-timeout-ms:60}")
    private int connectionTimeoutSec;

    @Value("${salesforce.response-timeout-ms:30}")
    private int responseTimeoutSec;

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

            PrivateKey privateKey;
            try {
                privateKey = readPrivateKey(bearerTokenPrivateKey);
            } catch (GeneralSecurityException e) {
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
    @Qualifier("SFWebClient")
    WebClient webClient(@Qualifier("salesforceAuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
                        OAuth2AuthorizationFailureHandler failureHandler) {

        HttpClient httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(responseTimeoutSec))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutSec * 1000);
        ServletOAuth2AuthorizedClientExchangeFilterFunction oAuth2Filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oAuth2Filter.setDefaultClientRegistrationId("sfclient");
        oAuth2Filter.setAuthorizationFailureHandler(failureHandler);
        // changing the ObjectMapper used by WebClient (if ever needed): https://stackoverflow.com/a/64105246/1207540

        return WebClient.builder()
                .filter(oAuth2Filter)
                // below for troubleshooting when needed
                // note request logging will log sensitive headers (JWTs etc.) so not good to run in production.
                //.filter(WebClientFilter.logRequest())
                //.filter(WebClientFilter.logResponse())
                .filter(WebClientFilter.handleErrors())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // See https://www.baeldung.com/java-read-pem-file-keys
    // other option: https://stackoverflow.com/a/49753179
    private static RSAPrivateKey readPrivateKey(String pemEncodedString) throws GeneralSecurityException {
        String privateKeyPEM = pemEncodedString
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PRIVATE KEY-----", "");

        byte[] encoded = Base64.decodeBase64(privateKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

}
