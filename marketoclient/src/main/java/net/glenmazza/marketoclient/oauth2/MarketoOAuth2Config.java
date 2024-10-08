package net.glenmazza.marketoclient.oauth2;

import io.netty.channel.ChannelOption;
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
import org.springframework.security.oauth2.client.ClientCredentialsOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizationSuccessHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.RemoveAuthorizedClientOAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


/**
 * May need to adjust responseBufferMB based on size of API responses
 */
@Configuration
@EnableConfigurationProperties({OAuth2ClientProperties.class})
@ConditionalOnProperty(name = "marketo.client.enabled")
public class MarketoOAuth2Config {

    private static final Logger LOGGER = LoggerFactory.getLogger(MarketoOAuth2Config.class);

    @Value("${marketo.connection-timeout-ms:60}")
    private int connectionTimeoutSec;

    @Value("${marketo.response-timeout-ms:30}")
    private int responseTimeoutSec;

    // 500 appears to be library default
    @Value("${marketo.maxconnections:500}")
    private int maxConnections;

    @Value("${marketo.maxidletime.sec:300}")
    private int maxIdleTimeSec;

    // 0 = disabled (library default) (0 = eviction relies on maxIdle, maxLifetime)
    @Value("${marketo.evictInBackground.sec:0}")
    private int evictInBackgroundTimeSec;

    @Value("${marketo.max.response.size.mb:2}")
    private int responseBufferMB;

    @Bean("marketoClient_authorizedClientManager")
    public OAuth2AuthorizedClientManager authorizedClientManager(ClientRegistrationRepository clientRegistrationRepository,
                                                                    OAuth2AuthorizedClientService authorizedClientService,
                                                                    @Qualifier("marketoClient_authorizationSuccessHandler")
                                                                     OAuth2AuthorizationSuccessHandler successHandler,
                                                                    @Qualifier("marketoClient_authorizationFailureHandler")
                                                                     OAuth2AuthorizationFailureHandler failureHandler) {


        OAuth2AuthorizedClientProvider clientProvider = new ClientCredentialsOAuth2AuthorizedClientProvider();

        Map<String, Object> accessTokenRequestProperties = new HashMap<>();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(clientProvider);
        authorizedClientManager.setContextAttributesMapper(request -> accessTokenRequestProperties);
        authorizedClientManager.setAuthorizationSuccessHandler(successHandler);
        authorizedClientManager.setAuthorizationFailureHandler(failureHandler);

        return authorizedClientManager;
    }

    @Bean("marketoClient_authorizationSuccessHandler")
    public OAuth2AuthorizationSuccessHandler authorizationSuccessHandler(OAuth2AuthorizedClientService authorizedClientService) {
        return (authorizedClient, principal, attributes) -> {
            OAuth2AccessToken token = authorizedClient.getAccessToken();
            LOGGER.info("Storing access token {}..., issued {}, expires {} ({} secs good)",
                    token.getTokenValue().substring(0, 10),
                    token.getIssuedAt(), token.getExpiresAt(),
                    token.getIssuedAt() != null ?
                            Duration.between(token.getIssuedAt(), token.getExpiresAt()).getSeconds() : "???");
            authorizedClientService.saveAuthorizedClient(authorizedClient, principal);
        };
    }

    /** Using a single failure handler between the AuthorizedClientManager (which adds client access
     *  tokens) and the ServletOAuth2AuthorizedClientExchangeFilterFunction (which
     */
    @Bean("marketoClient_authorizationFailureHandler")
    public OAuth2AuthorizationFailureHandler authorizationFailureHandler(OAuth2AuthorizedClientService authorizedClientService) {
        return new RemoveAuthorizedClientOAuth2AuthorizationFailureHandler(
                (clientRegistrationId, principal, attributes) ->
                        authorizedClientService.removeAuthorizedClient(clientRegistrationId, principal.getName()));
    }

    /**
     * Can use this WebClient directly if wishing to have "standard" error handling,
     * otherwise wire in WebClientBuilder and add a custom error handling filter.
     */
    @Bean("marketoClient_webClient")
    WebClient webClient(@Qualifier("marketoClient_webClientBuilder") WebClient.Builder builder) {
        return builder
                .filter(WebClientFilter.handleErrors(WebClientFilter::getMonoClientResponse))
                .build();
    }

    @Bean("marketoClient_webClientBuilder")
    WebClient.Builder webClientBuilder(@Qualifier("marketoClient_authorizedClientManager")
                                       OAuth2AuthorizedClientManager authorizedClientManager,
                                       @Qualifier("marketoClient_authorizationFailureHandler")
                                       OAuth2AuthorizationFailureHandler failureHandler) {
        return webClientBuilderGenerator(authorizedClientManager, failureHandler);
    }

    private WebClient.Builder webClientBuilderGenerator(OAuth2AuthorizedClientManager authorizedClientManager,
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
        oAuth2Filter.setDefaultClientRegistrationId("marketoclient");
        oAuth2Filter.setAuthorizationFailureHandler(failureHandler);
        // changing the ObjectMapper used by WebClient (if ever needed): https://stackoverflow.com/a/64105246/1207540

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // removes access token in case of 401s/403s, forcing re-authorization
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

}
