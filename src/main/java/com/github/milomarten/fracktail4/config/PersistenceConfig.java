package com.github.milomarten.fracktail4.config;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail4.persistence.AWSPersistence;
import com.github.milomarten.fracktail4.persistence.FilePersistence;
import com.github.milomarten.fracktail4.persistence.MultiPersistence;
import com.github.milomarten.fracktail4.persistence.Persistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class PersistenceConfig {
    @Bean
    public Persistence getPersistence(ObjectMapper om, AmazonS3 s3) {
        var aws = new AWSPersistence(s3, om);
        var file = new FilePersistence(om, "fracktail-files");
        return new MultiPersistence(List.of(file, aws));
    }
}
