package com.github.milomarten.fracktail.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PokeApiConfig {
    @Bean
    public WebClient graphQL() {
        return WebClient.builder()
                .baseUrl("https://graphql.pokeapi.co/v1beta2/query")
                .build();
    }
}
