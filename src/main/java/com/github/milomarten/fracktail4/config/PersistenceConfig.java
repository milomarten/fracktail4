package com.github.milomarten.fracktail4.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail4.persistence.FilePersistence;
import com.github.milomarten.fracktail4.persistence.Persistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {
    @Bean
    public Persistence getPersistence(ObjectMapper om) {
        return new FilePersistence(om, "fracktail-files");
    }
}
