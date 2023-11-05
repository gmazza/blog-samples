package net.glenmazza.marketoclient.marketoclient.model;

/**
 * Within Marketo, for <a href="https://developers.marketo.com/rest-api/error-codes/">response-level</a>
 * error codes.
 * @see ServiceException for Marketo HTTP-level error codes.
 */
public class ResponseError {

    String code;

    String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
