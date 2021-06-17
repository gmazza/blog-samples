package net.glenmazza.sfclient.model;

/**
 * https://careydevelopment.us/blog/spring-webflux-how-to-handle-errors-with-webclient
 * Sample exceptions:
 * Status Code 404: [{"errorCode":"NOT_FOUND","message":"Provided external ID field does not exist or is not accessible: 51"}]
 * Status Code 400: [{"message":"No such column 'abcd' on sobject of type Account","errorCode":"INVALID_FIELD"}]
 */
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -7661881974219233311L;

    private final int statusCode;

    public ServiceException(String message, int statusCode) {
        super(String.format("Status Code %d: %s", statusCode, message));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
