package com.github.milomarten.fracktail4.persistence;

import reactor.core.publisher.Mono;

public interface Persistence {
    Mono<Void> store(String key, Object value);

    <T> Mono<T> retrieve(String key, Class<T> clazz);

    Mono<Boolean> hasKey(String key);
}
