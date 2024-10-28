package com.github.milomarten.fracktail4.amtrak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AmtrakConfig {
    @Bean
    public WebClient amtrakWebClient() {
        return WebClient.create("https://api-v3.amtraker.com/v3");
    }
}
