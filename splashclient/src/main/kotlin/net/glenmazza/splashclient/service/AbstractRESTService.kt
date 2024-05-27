package net.glenmazza.splashclient.service

import com.fasterxml.jackson.databind.ObjectMapper
import net.glenmazza.splashclient.util.createObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.reactive.function.client.WebClient

abstract class AbstractRESTService(val webClient: WebClient) {

    @Value("\${splash.api.base-url}")
    protected var baseUrl: String? = null

    protected val objectMapper: ObjectMapper? = createObjectMapper()
}
