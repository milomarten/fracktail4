package com.github.milomarten.fracktail4.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

/**
 * A generic persistence layer, to store and retrieve objects from some store, such as a filesystem.
 */
public interface Persistence {
    /**
     * Persist some object
     * @param key The persistence key. Can be a filename, S3 bucket, etc
     * @param value The object to store.
     * @return A Mono which indicates completion or an error.
     */
    Mono<Void> store(String key, Object value);

    /**
     * Retrieve an object from the persistence layer
     * @param key The persistence key.
     * @param clazz The type of object in the persistence layer
     * @return The object from persistence, or an empty Mono if not present.
     * @param <T> The type of object to retrieve from.
     */
    <T> Mono<T> retrieve(String key, Class<T> clazz);

    /**
     * Retrieve an object from the persistence layer
     * @param key The persistence key.
     * @param clazz The type of object in the persistence layer
     * @return The object from persistence, or an empty Mono if not present.
     * @param <T> The type of object to retrieve from.
     */
    <T> Mono<T> retrieve(String key, TypeReference<T> clazz);

    /**
     * Check if a persistence key exists
     * @param key The key to check for
     * @return True, if persistence has a key of this type, false otherwise.
     */
    Mono<Boolean> hasKey(String key);
}
