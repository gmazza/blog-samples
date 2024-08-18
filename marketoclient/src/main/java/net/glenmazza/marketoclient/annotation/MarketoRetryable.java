package net.glenmazza.marketoclient.annotation;

import net.glenmazza.marketoclient.exception.MarketoAccessTokenExpiredException;
import net.glenmazza.marketoclient.exception.MarketoTooFrequentRequestsException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Retryable(value = {MarketoAccessTokenExpiredException.class, MarketoTooFrequentRequestsException.class},
        maxAttempts = 4, backoff = @Backoff(delay = 2000, multiplier = 4))
public @interface MarketoRetryable {
}
