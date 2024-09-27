package com.github.milomarten.fracktail4.persistence;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class AWSPersistence implements Persistence {
    private static final String BUCKET_NAME = "fracktail-persistence";

    private final AmazonS3 s3;
    private final ObjectMapper mapper;

    @Override
    public Mono<Void> store(String key, Object value) {
        return Mono.fromCallable(() -> {
            String contents = mapper.writeValueAsString(value);
            s3.putObject(BUCKET_NAME, key, contents);
            log.info("Storing in AWS bucket {}, obj {}", BUCKET_NAME, key);
            return null;
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return hasKey(key)
                .flatMap(b -> b ? Mono.just(key) : Mono.empty())
                .flatMap(rKey -> {
                    var object = s3.getObject(BUCKET_NAME, rKey);
                    log.info("Retrieving from AWS bucket {}, obj {}", BUCKET_NAME, key);
                    try {
                        return Mono.just(mapper.readValue(object.getObjectContent(), clazz));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return hasKey(key)
                .flatMap(b -> b ? Mono.just(key) : Mono.empty())
                .flatMap(rKey -> {
                    var object = s3.getObject(BUCKET_NAME, rKey);
                    log.info("Retrieving from AWS bucket {}, obj {}", BUCKET_NAME, key);
                    try {
                        return Mono.just(mapper.readValue(object.getObjectContent(), clazz));
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Mono.fromCallable(() -> s3.doesObjectExist(BUCKET_NAME, key));
    }
}
