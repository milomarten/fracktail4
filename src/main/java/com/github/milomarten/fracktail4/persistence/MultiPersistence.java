package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * A fallback-chain type of Persistence that checks multiple places.
 * Retrieval will sequentially check each persistence supplied until one with the requested
 * object is present, which will be returned.
 * Storing will store in all persistence levels.
 * This is useful for migrating between persistence layers.
 */
@RequiredArgsConstructor
public class MultiPersistence implements Persistence {
    private final List<Persistence> persistences;

    @Override
    public Mono<Void> store(String key, Object value) {
        return Flux.fromIterable(persistences)
                .flatMap(p -> p.store(key, value))
                .then();
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return Flux.fromIterable(persistences)
                .filterWhen(p -> p.hasKey(key))
                .next()
                .flatMap(p -> p.retrieve(key, clazz));
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return Flux.fromIterable(persistences)
                .filterWhen(p -> p.hasKey(key))
                .next()
                .flatMap(p -> p.retrieve(key, clazz));
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Flux.fromIterable(this.persistences)
                .flatMap(p -> p.hasKey(key))
                .any(p -> p);
    }
}
