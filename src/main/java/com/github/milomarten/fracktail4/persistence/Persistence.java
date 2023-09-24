package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

public interface Persistence {
    Mono<Void> store(String key, Object value);

    <T> Mono<T> retrieve(String key, Class<T> clazz);

    <T> Mono<T> retrieve(String key, TypeReference<T> clazz);

    Mono<Boolean> hasKey(String key);
}
