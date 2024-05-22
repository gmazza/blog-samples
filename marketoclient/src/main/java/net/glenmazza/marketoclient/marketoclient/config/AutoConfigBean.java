package net.glenmazza.marketoclient.marketoclient.config;

import net.glenmazza.marketoclient.marketoclient.oauth2.MarketoOAuth2Config;
import net.glenmazza.marketoclient.marketoclient.service.ActivityService;
import net.glenmazza.marketoclient.marketoclient.service.BulkExtractService;
import net.glenmazza.marketoclient.marketoclient.service.LeadService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(value = "marketo.client.enabled")
@Import({MarketoOAuth2Config.class})
public class AutoConfigBean {

    // instantiate as @Beans the API-calling objects in the service package
    // use Qualifier so no conflicts with WebClients in other packages
    @Bean
    public LeadService leadService(@Qualifier("marketoClient_webClient") WebClient webClient,
                                   @Qualifier("marketoClient_authorizationFailureHandler") OAuth2AuthorizationFailureHandler failureHandler) {
        return new LeadService(webClient, failureHandler);
    }

    @Bean
    public ActivityService activityService(@Qualifier("marketoClient_webClient") WebClient webClient,
                                           @Qualifier("marketoClient_authorizationFailureHandler") OAuth2AuthorizationFailureHandler failureHandler) {
        return new ActivityService(webClient, failureHandler);
    }

    @Bean
    public BulkExtractService bulkExtractService(@Qualifier("marketoClient_webClient") WebClient webClient,
                                                 @Qualifier("marketoClient_authorizationFailureHandler") OAuth2AuthorizationFailureHandler failureHandler) {
        return new BulkExtractService(webClient, failureHandler);
    }
}
