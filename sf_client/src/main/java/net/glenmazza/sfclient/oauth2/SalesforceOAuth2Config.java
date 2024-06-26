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
import reactor.netty.resources.ConnectionProvider;

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

    // 500 appears to be library default
    @Value("${salesforce.maxconnections:500}")
    private int maxConnections;

    @Value("${salesforce.maxidletime.sec:300}")
    private int maxIdleTimeSec;

    // 0 = disabled (library default) (0 = eviction relies on maxIdle, maxLifetime)
    @Value("${salesforce.evictInBackground.sec:0}")
    private int evictInBackgroundTimeSec;

    @Value("${salesforce.max.response.size.mb:2}")
    private int responseBufferMB;

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

    /**
     * Can use this WebClient directly if wishing to have "standard" error handling,
     * otherwise wire in WebClientBuilder and add a custom error handling filter.
     */
    @Bean
    @Qualifier("SFWebClient")
    WebClient webClient(WebClient.Builder builder) {
        return builder
                .filter(WebClientFilter.handleErrors(WebClientFilter::getMonoClientResponse))
                .build();
    }

    @Bean
    WebClient.Builder webClientBuilder(@Qualifier("salesforceAuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
                                       OAuth2AuthorizationFailureHandler failureHandler) {
        return webClientBuilderGenerator(authorizedClientManager, failureHandler);
    }

    private WebClient.Builder webClientBuilderGenerator(@Qualifier("salesforceAuthorizedClientManager") OAuth2AuthorizedClientManager authorizedClientManager,
                                                        OAuth2AuthorizationFailureHandler failureHandler) {

        // 401's are solved by repeating the call (.retry(1)), but sometimes fails (1x/2x per day), error message:
        // "The connection observed an error, the request cannot be retried as the headers/body
        // were sent io.netty.channel.unix.Errors$NativeIoException: readAddress(..) failed: Connection reset by peer"
        // Using solution: https://github.com/reactor/reactor-netty/issues/1774#issuecomment-908066283
        // setting maxIdleTime in particular seems most important
        ConnectionProvider provider = ConnectionProvider.builder("fixed")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTimeSec))
                // not using yet
                //.maxLifeTime(Duration.ofSeconds(maxLifeTimeSec))
                //.pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(evictInBackgroundTimeSec)).build();

        // To log req & res: https://www.baeldung.com/spring-log-webclient-calls#logging-request-repsonse
        HttpClient httpClient = HttpClient.create(provider).responseTimeout(Duration.ofSeconds(responseTimeoutSec))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutSec * 1000);

        ServletOAuth2AuthorizedClientExchangeFilterFunction oAuth2Filter =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oAuth2Filter.setDefaultClientRegistrationId("sfclient");
        oAuth2Filter.setAuthorizationFailureHandler(failureHandler);
        // changing the ObjectMapper used by WebClient (if ever needed): https://stackoverflow.com/a/64105246/1207540

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(oAuth2Filter)
                // raise default 256K message limit to 2MB (https://stackoverflow.com/a/59392022/1207540)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(responseBufferMB * 1024 * 1024));
        // below two helpful when debugging
        //.filter(WebClientFilter.logRequest())
        //.filter(WebClientFilter.logResponse())
        // wraps non-401s into ServiceExceptions for client
        // (401s passed-thru as Spring uses that as a signal to request a new access token)
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
