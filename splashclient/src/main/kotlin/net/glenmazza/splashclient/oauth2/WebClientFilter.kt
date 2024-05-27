package net.glenmazza.splashclient.oauth2

import net.glenmazza.splashclient.model.ServiceException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.util.function.Consumer
import java.util.function.Function

// Adapted from https://careydevelopment.us/blog/spring-webflux-how-to-log-responses-with-webclient
// https://github.com/careydevelopment/contact-service/blob/0.3.7-webclient-response-logging/src/main/java/com/careydevelopment/contact/service/WebClientFilter.java

private val LOG: Logger = LoggerFactory.getLogger("WebClientFilter")

fun logRequest(): ExchangeFilterFunction? {
    return ExchangeFilterFunction.ofRequestProcessor { request: ClientRequest ->
        logMethodAndUrl(request)
        logHeaders(request)
        Mono.just(request)
    }
}

fun logResponse(): ExchangeFilterFunction? {
    return ExchangeFilterFunction.ofResponseProcessor { response: ClientResponse ->
        logStatus(response)
        logHeaders(response)
        Mono.just(response)
    }
}

private fun logStatus(response: ClientResponse) {
    val status = response.statusCode()
    LOG.info("Returned status code {} ({})", status.value(), status.reasonPhrase)
}

/**
 * Different API calls return different 400-series exception objects, but regardless 401 codes need to be passed through
 * to inform Spring to get a new access token.
 *
 * @param monoCRFunction - the response object to return in case of a non-401 and non-500 error
 * @return
 */
fun handleErrors(monoCRFunction: Function<ClientResponse, Mono<ClientResponse>>): ExchangeFilterFunction {
    return ExchangeFilterFunction.ofResponseProcessor { response: ClientResponse ->
        val status = response.statusCode()
        // don't wrap 401 errors, as Spring uses that code to delete the expired access token and request a new one
        if (status.is4xxClientError && status.value() != 401 || status.is5xxServerError) {
            return@ofResponseProcessor monoCRFunction.apply(response)
        } else {
            return@ofResponseProcessor Mono.just<ClientResponse>(response)
        }
    }
}

fun getMonoClientResponse(response: ClientResponse): Mono<ClientResponse> {
    val status = response.statusCode()
    return response.bodyToMono(String::class.java) // defaultIfEmpty:  401's, 403's, etc. sometimes return null body
        // https://careydevelopment.us/blog/spring-webflux-how-to-handle-empty-responses
        .defaultIfEmpty(status.reasonPhrase)
        .flatMap { body: String? ->
            LOG.info(
                "Error status code {} ({}) Response Body: {}",
                status.value(),
                status.reasonPhrase,
                body
            )
            Mono.error(ServiceException(body, response.rawStatusCode()))
        }
}

private fun logHeaders(response: ClientResponse) {
    response.headers().asHttpHeaders().forEach { name: String, values: List<String> ->
        values.forEach(
            Consumer { value: String ->
                logNameAndValuePair(name, value)
            }
        )
    }
}

private fun logHeaders(request: ClientRequest) {
    request.headers().forEach { name: String, values: List<String> ->
        values.forEach(
            Consumer { value: String ->
                logNameAndValuePair(name, value)
            }
        )
    }
}

private fun logNameAndValuePair(name: String, value: String) {
    LOG.info("header {}={}", name, value)
}

private fun logMethodAndUrl(request: ClientRequest) {
    val sb = StringBuilder()
    sb.append(request.method().name)
    sb.append(" to ")
    sb.append(request.url())
    LOG.info(sb.toString())
}
