package net.glenmazza.sfclient.oauth2;

import net.glenmazza.sfclient.model.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import reactor.core.publisher.Mono;

// Adapted from https://careydevelopment.us/blog/spring-webflux-how-to-log-responses-with-webclient
// https://github.com/careydevelopment/contact-service/blob/0.3.7-webclient-response-logging/src/main/java/com/careydevelopment/contact/service/WebClientFilter.java
public class WebClientFilter {

    private static final Logger LOG = LoggerFactory.getLogger(WebClientFilter.class);

    // logging request and response bodies more complex due to once-only consumption rules
    // i.e., body no longer available downstream. However logging headers & status code fine.
    // options on logging body:
    // https://www.baeldung.com/spring-log-webclient-calls#logging-request-repsonse
    // https://stackoverflow.com/a/55790675/1207540
    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logMethodAndUrl(request);
            logHeaders(request);

            return Mono.just(request);
        });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            logStatus(response);
            logHeaders(response);

            return Mono.just(response);
        });
    }

    private static void logStatus(ClientResponse response) {
        HttpStatus status = response.statusCode();
        LOG.debug("Returned status code {} ({})", status.value(), status.getReasonPhrase());
    }

    public static ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            HttpStatus status = response.statusCode();
            // don't wrap 401 errors, as Spring uses that code to delete expired access token and request new one
            if ((status.is4xxClientError() && status.value() != 401) || status.is5xxServerError()) {
                return response.bodyToMono(String.class)
                        // defaultIfEmpty:  401's, 403's, etc. sometimes return null body
                        // https://careydevelopment.us/blog/spring-webflux-how-to-handle-empty-responses
                        .defaultIfEmpty(response.statusCode().getReasonPhrase())
                        .flatMap(body -> {
                            LOG.info("Error status code {} ({}) Response Body: {}", status.value(),
                                    status.getReasonPhrase(), body);
                            // return Mono.just(response); <-- throws WebClient exception back to client instead
                            return Mono.error(new ServiceException(body, response.rawStatusCode()));
                        });
            } else {
                return Mono.just(response);
            }
        });
    }

    private static void logHeaders(ClientResponse response) {
        response.headers().asHttpHeaders().forEach((name, values) -> {
            values.forEach(value -> {
                logNameAndValuePair(name, value);
            });
        });
    }

    private static void logHeaders(ClientRequest request) {
        request.headers().forEach((name, values) -> {
            values.forEach(value -> {
                logNameAndValuePair(name, value);
            });
        });
    }

    private static void logNameAndValuePair(String name, String value) {
        LOG.debug("{}={}", name, value);
    }

    private static void logMethodAndUrl(ClientRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append(request.method().name());
        sb.append(" to ");
        sb.append(request.url());

        LOG.debug(sb.toString());
    }
}
