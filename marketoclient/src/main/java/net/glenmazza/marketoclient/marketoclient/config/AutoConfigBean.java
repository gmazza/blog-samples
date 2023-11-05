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
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(value = "marketo.client.enabled")
@Import({MarketoOAuth2Config.class})
public class AutoConfigBean {

    // instantiate as @Beans the API-calling objects in the service package
    // use Qualifier so no conflicts with WebClients in other packages
    @Bean
    public LeadService leadService(@Qualifier("marketoClient_webClient") WebClient webClient) {
        return new LeadService(webClient);
    }

    @Bean
    public ActivityService activityService(@Qualifier("marketoClient_webClient") WebClient webClient) {
        return new ActivityService(webClient);
    }

    @Bean
    public BulkExtractService bulkExtractService(@Qualifier("marketoClient_webClient") WebClient webClient) {
        return new BulkExtractService(webClient);
    }
}
