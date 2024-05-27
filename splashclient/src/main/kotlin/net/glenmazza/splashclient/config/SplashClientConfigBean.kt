package net.glenmazza.splashclient.config

import net.glenmazza.splashclient.oauth2.ClientOAuth2Config
import net.glenmazza.splashclient.service.SplashQueryRunner
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnProperty(value = ["splash.client.enabled"])
@Import(
    ClientOAuth2Config::class
)
class SplashClientConfigBean {
    @Bean
    fun splashQueryRunner(
        @Qualifier("splashClient_webClient") webClient: WebClient,
        @Value("\${splash.api.base-url}") apiUrl: String
    ): SplashQueryRunner {
        return SplashQueryRunner(webClient, apiUrl)
    }
}
