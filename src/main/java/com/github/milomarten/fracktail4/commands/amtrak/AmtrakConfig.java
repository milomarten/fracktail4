package com.github.milomarten.fracktail4.commands.amtrak;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AmtrakConfig {
    @Bean
    public WebClient amtrakWebClient(ObjectMapper om) {
        return WebClient.builder()
                .baseUrl("https://api-v3.amtraker.com/v3")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(codecs -> {
                            codecs.defaultCodecs()
                                    .maxInMemorySize((int) DataSize.ofMegabytes(32).toBytes());
                            codecs.defaultCodecs()
                                    .jackson2JsonEncoder(new Jackson2JsonEncoder(om, MediaType.APPLICATION_JSON));
                            codecs.defaultCodecs()
                                    .jackson2JsonDecoder(new Jackson2JsonDecoder(om, MediaType.APPLICATION_JSON));
                        })
                        .build())
                .build();
    }
}
