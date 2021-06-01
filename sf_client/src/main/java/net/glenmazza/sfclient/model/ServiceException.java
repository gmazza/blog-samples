package net.glenmazza.sfclient.model;

// https://careydevelopment.us/blog/spring-webflux-how-to-handle-errors-with-webclient
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = -7661881974219233311L;

    private final int statusCode;

    public ServiceException (String message, int statusCode) {
        super(String.format("Status Code %d: %s", statusCode, message));
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}