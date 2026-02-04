package ai.langgraph4j.aiagent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "intellicon")
    @Primary
    public WebClient getIntelliconWebClient() {
        return WebClient.builder()
                .baseUrl("http://49.247.30.24:5014")
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs()
                                        .maxInMemorySize(50 * 1024 * 1024))
                                .build())
                .build();
    }

    @Bean(name = "taxnetapi")
    public WebClient getTaxnetApiWebClient() {
        return WebClient.builder().baseUrl("https://mining.taxnet.co.kr/taxnetapi")
                .exchangeStrategies(
                        ExchangeStrategies.builder()
                                .codecs(c -> c.defaultCodecs()
                                        .maxInMemorySize(50 * 1024 * 1024))
                                .build())
                .build();
    }

}
