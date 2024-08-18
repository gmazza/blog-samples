package net.glenmazza.marketoclient.service;

import net.glenmazza.marketoclient.annotation.MarketoRetryable;
import net.glenmazza.marketoclient.model.activities.ActivityTypeResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;

public class ActivityService extends AbstractRESTService {

    public ActivityService(WebClient webClient, OAuth2AuthorizationFailureHandler failureHandler) {
        super(webClient, failureHandler);
    }

    @MarketoRetryable
    public ActivityTypeResponse getActivityTypes() {
        ActivityTypeResponse atr = getActivityTypesInternal();
        checkForMarketoErrorResponses(atr);
        return atr;
    }

    private ActivityTypeResponse getActivityTypesInternal() {
        return webClient
                .get()
                .uri(baseUrl + "/rest/v1/activities/types.json")
                .retrieve()
                .bodyToMono(ActivityTypeResponse.class)
                // retry of 1: if access token expired, will be removed after
                // first failed call and obtained & used during second.
                .retry(1)
                .block();

    }
}
