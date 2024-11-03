package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.util.function.UnaryOperator;

public abstract class PersistingObject<T> {
    private final TypeReference<T> typeReference;
    private T current;
    private final Persistence persistence;
    @Getter private final String key;

    public PersistingObject(Persistence persistence, String key) {
        this.typeReference = new TypeReference<T>() {};
        this.persistence = persistence;
        this.key = key;
    }

    protected abstract T createInitial();

    @PostConstruct
    protected void setUp() {
        this.current = this.persistence.hasKey(key)
                .flatMap(has -> {
                    if (has) {
                        return this.persistence.retrieve(key, typeReference);
                    } else {
                        return Mono.fromSupplier(this::createInitial);
                    }
                })
                .block();
    }

    public T get() {
        return this.current;
    }

    public Mono<Void> update(UnaryOperator<T> updater) {
        this.current = updater.apply(this.current);
        return this.persistence.store(this.key, this.current);
    }

    public Mono<Void> set(T newValue) {
        this.current = newValue;
        return this.persistence.store(this.key, this.current);
    }
}
