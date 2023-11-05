package net.glenmazza.marketoclient.marketoclient.service;

import net.glenmazza.marketoclient.marketoclient.model.activities.ActivityTypeResponse;
import org.springframework.web.reactive.function.client.WebClient;

public class ActivityService extends AbstractRESTService {

    public ActivityService(WebClient webClient) {
        super(webClient);
    }

    public ActivityTypeResponse getActivityTypes() {
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
