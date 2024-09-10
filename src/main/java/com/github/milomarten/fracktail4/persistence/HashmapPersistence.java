package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class HashmapPersistence implements Persistence {
    private final Map<String, String> store = new HashMap<>();
    private final ObjectMapper om;

    @Override
    public Mono<Void> store(String key, Object value) {
        return Mono.fromCallable(() -> store.put(key, om.writeValueAsString(value)))
                .then();
    }

    @Override
    public <T> Mono<T> retrieve(String key, Class<T> clazz) {
        return Mono.fromCallable(() -> {
            if (store.containsKey(key)) {
                return om.readValue(store.get(key), clazz);
            } else {
                return null;
            }
        });
    }

    @Override
    public <T> Mono<T> retrieve(String key, TypeReference<T> clazz) {
        return Mono.fromCallable(() -> {
            if (store.containsKey(key)) {
                return om.readValue(store.get(key), clazz);
            } else {
                return null;
            }
        });
    }

    @Override
    public Mono<Boolean> hasKey(String key) {
        return Mono.fromSupplier(() -> store.containsKey(key));
    }
}
