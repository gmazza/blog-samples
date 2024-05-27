package net.glenmazza.splashclient.oauth2

import io.netty.channel.ChannelOption
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.RemoveAuthorizedClientOAuth2AuthorizationFailureHandler
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.time.Duration

@Configuration
@EnableConfigurationProperties(OAuth2ClientProperties::class)
@ConditionalOnProperty(name = ["splash.client.enabled"])
class ClientOAuth2Config {

    private val LOGGER: Logger =
        LoggerFactory.getLogger(ClientOAuth2Config::class.java)

    @Value("\${splash.oauth2.resourceowner.username}")
    private val username: String? = null

    @Value("\${splash.oauth2.resourceowner.password}")
    private val password: String? = null

    @Value("\${splash.connection-timeout-ms:60}")
    private val connectionTimeoutSec = 0

    @Value("\${splash.response-timeout-ms:30}")
    private val responseTimeoutSec = 0

    // 500 appears to be library default
    @Value("\${splash.maxconnections:500}")
    private val maxConnections = 0

    @Value("\${splash.maxidletime.sec:300}")
    private val maxIdleTimeSec = 0

    @Value("\${splash.max.response.size.mb:3}")
    private val responseBufferMB = 0

    // 0 = disabled (library default) (0 = eviction relies on maxIdle, maxLifetime)
    @Value("\${splash.evictInBackground.sec:0}")
    private val evictInBackgroundTimeSec = 0

    @Bean("splashClient_authorizedClientManager")
    fun authorizedClientManager(
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
        @Qualifier("splashClient_authorizationFailureHandler") failureHandler: OAuth2AuthorizationFailureHandler?
    ): OAuth2AuthorizedClientManager {
        val clientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
            .password()
            .build()

        val accessTokenRequestProperties = mutableMapOf<String, Any>()

        // username needed for both grant methods supported
        accessTokenRequestProperties[OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME] = username!!
        accessTokenRequestProperties[OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME] = password!!

        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)
        authorizedClientManager.setAuthorizedClientProvider(clientProvider)
        authorizedClientManager.setContextAttributesMapper { _: OAuth2AuthorizeRequest? -> accessTokenRequestProperties }
        authorizedClientManager.setAuthorizationFailureHandler(failureHandler)
        return authorizedClientManager
    }

    /** Using a single failure handler between the AuthorizedClientManager (which adds client access
     * tokens) and the ServletOAuth2AuthorizedClientExchangeFilterFunction (which
     */
    @Bean("splashClient_authorizationFailureHandler")
    fun authorizationFailureHandler(authorizedClientService: OAuth2AuthorizedClientService): OAuth2AuthorizationFailureHandler {
        return RemoveAuthorizedClientOAuth2AuthorizationFailureHandler {
                clientRegistrationId: String?, principal: Authentication, _: Map<String?, Any?>? ->
            authorizedClientService.removeAuthorizedClient(
                clientRegistrationId,
                principal.name
            )
        }
    }

    /**
     * Can use this WebClient directly if wishing to have "standard" error handling,
     * otherwise wire in WebClientBuilder and add a custom error handling filter.
     */
    @Bean("splashClient_webClient")
    fun webClient(@Qualifier("splashClient_webClientBuilder") builder: WebClient.Builder): WebClient? {
        return builder
            .filter(handleErrors(::getMonoClientResponse))
            .build()
    }

    @Bean("splashClient_webClientBuilder")
    fun webClientBuilder(
        @Qualifier("splashClient_authorizedClientManager") authorizedClientManager: OAuth2AuthorizedClientManager,
        @Qualifier("splashClient_authorizationFailureHandler") failureHandler: OAuth2AuthorizationFailureHandler
    ): WebClient.Builder? {
        return webClientBuilderGenerator(authorizedClientManager, failureHandler)
    }

    private fun webClientBuilderGenerator(
        authorizedClientManager: OAuth2AuthorizedClientManager,
        failureHandler: OAuth2AuthorizationFailureHandler
    ): WebClient.Builder {
        // 401's are solved by repeating the call (.retry(1)), but sometimes fails (1x/2x per day), error message:
        // "The connection observed an error, the request cannot be retried as the headers/body
        // were sent io.netty.channel.unix.Errors$NativeIoException: readAddress(..) failed: Connection reset by peer"
        // Using solution: https://github.com/reactor/reactor-netty/issues/1774#issuecomment-908066283
        // setting maxIdleTime in particular seems most important
        val provider = ConnectionProvider.builder("fixed")
            .maxConnections(maxConnections)
            .maxIdleTime(Duration.ofSeconds(maxIdleTimeSec.toLong())) // not using yet
            .evictInBackground(Duration.ofSeconds(evictInBackgroundTimeSec.toLong())).build()

        // To log req & res: https://www.baeldung.com/spring-log-webclient-calls#logging-request-repsonse
        val httpClient = HttpClient.create(provider).responseTimeout(Duration.ofSeconds(responseTimeoutSec.toLong()))
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeoutSec * 1000)
        val oAuth2Filter = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oAuth2Filter.setDefaultClientRegistrationId("splashclient")
        oAuth2Filter.setAuthorizationFailureHandler(failureHandler)
        // changing the ObjectMapper used by WebClient (if ever needed): https://stackoverflow.com/a/64105246/1207540
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient))
            .filter(oAuth2Filter)
            .codecs { configurer: ClientCodecConfigurer ->
                configurer
                    .defaultCodecs() // adjusting response buffer: (https://stackoverflow.com/a/59392022/1207540)
                    .maxInMemorySize(responseBufferMB * 1024 * 1024)
            }
        //  below helpful if debugging
        //  .filter(logRequest()!!)
        //  .filter(logResponse()!!)
    }
}
