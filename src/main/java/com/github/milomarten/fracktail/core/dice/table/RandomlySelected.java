package com.github.milomarten.fracktail.core.dice.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.Objects;
import java.util.OptionalInt;
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

    /**
     * Get the number of possible options
     * This may help with certain optimization purposes, or else just viewing the length of a given table.
     * If OptionalInt is empty, the number of options should be considered as infinite.
     * @return The sample size, or empty if infinite.
     */
    OptionalInt length();

    /**
     * Map the result of get() to another type
     * Nulls are passed through verbatim. If the mapper explicitly converts a legitimate value
     * into a null, a NullPointerException is thrown.
     * @param mapper The mapper to use
     * @return A RandomlySelected which uses mapper to change the result.
     * @param <U> The new type
     */
    default <U> RandomlySelected<U> map(Function<T, U> mapper) {
        var self = this;
        return new RandomlySelected<U>() {
            @Override
            public U get(UniformRandomProvider random) {
                var wrapped = self.get(random);
                return wrapped == null ? null : Objects.requireNonNull(mapper.apply(wrapped));
            }

            @Override
            public OptionalInt length() {
                return self.length();
            }
        };
    }

    /**
     * A "random collection" just containing one possible option.
     * @param content The content to serve whenever get() is called.
     * @param <T> The type of the content.
     */
    record Static<T>(T content) implements RandomlySelected<T> {
        private static final OptionalInt ONE = OptionalInt.of(1);

        @Override
        public T get(UniformRandomProvider random) {
            return content;
        }

        @Override
        public OptionalInt length() {
            return ONE;
        }
    }
}
