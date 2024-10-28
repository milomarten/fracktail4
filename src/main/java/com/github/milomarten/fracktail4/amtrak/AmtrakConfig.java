package com.github.milomarten.fracktail4.amtrak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AmtrakConfig {
    @Bean
    public WebClient amtrakWebClient() {
//        return WebClient.create("https://api-v3.amtraker.com/v3");
        return WebClient.builder()
                .baseUrl("https://api-v3.amtraker.com/v3")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize((int) DataSize.ofMegabytes(32).toBytes()))
                        .build())
                .build();
    }
}
