package com.github.milomarten.fracktail.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PokeApiConfig {
    @Bean
    public WebClient graphQL() {
        return WebClient.builder()
                .baseUrl("https://graphql.pokeapi.co/v1beta2/query")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize((int) DataSize.ofMegabytes(16).toBytes()))
                        .build())
                .build();
    }
}
