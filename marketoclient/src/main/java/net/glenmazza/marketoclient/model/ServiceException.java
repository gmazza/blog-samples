package net.glenmazza.marketoclient.model;

/**
 * <a href="https://careydevelopment.us/blog/spring-webflux-how-to-handle-errors-with-webclient">Tutorial for
 * HTTP errors with Spring WebClient</a>
 * Within Marketo, for <a href="https://developers.marketo.com/rest-api/error-codes/">HTTP-level</a>
 * error codes.
 * @see ResponseError for Marketo response-level error codes.
 */
public class ServiceException extends RuntimeException {

    private final int statusCode;

    public ServiceException(String message, int statusCode) {
        super(String.format("Status Code %d: %s", statusCode, message));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
