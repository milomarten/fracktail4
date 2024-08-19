package com.github.milomarten.fracktail4.persistence;

import com.amazonaws.services.s3.AmazonS3;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(AmazonS3.class)
public class AWSPersistence implements Persistence {
    private static final String BUCKET_NAME = "fracktail-persistence";

    private final AmazonS3 s3;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> store(String key, Object value) {
        return Mono.fromCallable(() -> {
            String contents = mapper.writeValueAsString(value);
            s3.putObject(BUCKET_NAME, key, contents);
            return null;
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            var object = s3.getObject(BUCKET_NAME, key);
            return mapper.readValue(object.getObjectContent(), clazz);
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return Mono.fromCallable(() -> {
            var object = s3.getObject(BUCKET_NAME, key);
            return mapper.readValue(object.getObjectContent(), clazz);
        });
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Mono.just(false);
    }
}
