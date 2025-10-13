package com.github.milomarten.fracktail.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail.core.persistence.FilePersistence;
import com.github.milomarten.fracktail.core.persistence.Persistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PersistenceConfig {
    @Bean
    public Persistence getPersistence(ObjectMapper om) {
        return new FilePersistence(om, "fracktail-files");
    }
}
