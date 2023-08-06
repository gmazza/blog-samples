package net.glenmazza.domoclient.config;

import net.glenmazza.domoclient.oauth2.DomoOAuth2Config;
import net.glenmazza.domoclient.service.DataSetQueryRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@ConditionalOnProperty(value = "domo.client.enabled")
@Import({DomoOAuth2Config.class})
public class AutoConfigBean {

    @Bean
    public DataSetQueryRunner dataSetQueryRunner(@Qualifier("domoClient_webClient") WebClient webClient) {
        return new DataSetQueryRunner(webClient);
    }
}
