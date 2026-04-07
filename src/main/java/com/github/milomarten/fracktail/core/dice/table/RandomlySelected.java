package com.github.milomarten.fracktail.core.dice.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.function.Function;

/**
 * Represents a collection from which something can be randomly selected.
 * @param <T> The "something" that can be randomly selected.
 */
public interface RandomlySelected<T> {
    /**
     * Retrieve an item randomly from this collection.
     * null should be returned if, and only if, this collection is empty.
     * @param random The source of randomness to use.
     * @return An item from this collection, randomly sourced.
     */
    T get(UniformRandomProvider random);

    default <U> RandomlySelected<U> map(Function<T, U> mapper) {
        var self = this;
        return random -> {
            var wrapped = self.get(random);
            return wrapped == null ? null : mapper.apply(wrapped);
        };
    }

    /**
     * A "random collection" just containing one possible option.
     * @param content The content to serve whenever get() is called.
     * @param <T> The type of the content.
     */
    record Static<T>(T content) implements RandomlySelected<T> {
        @Override
        public T get(UniformRandomProvider random) {
            return content;
        }
    }
}
