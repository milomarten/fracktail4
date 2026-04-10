package com.github.milomarten.fracktail.core.dice.table;

import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.OptionalInt;
import java.util.function.BiFunction;

/**
 * A RollTable which combines the output of two tables together
 * The contents of each RandomlySelected are queried and merged together using
 * the provided function. If either RandomlySelected returns null, the output is null.
 * @param <A> The type returned from the first RandomlySelected
 * @param <B> The type returned from the second RandomlySelected
 * @param <T> The type created by merging A and B
 */
@RequiredArgsConstructor
public class MergedTable<A, B, T> implements RandomlySelected<T> {
    private final RandomlySelected<A> a;
    private final RandomlySelected<B> b;
    private final BiFunction<A, B, T> merger;

    @Override
    public T get(UniformRandomProvider random) {
        var aGet = a.get(random);
        if (aGet == null) { return null; }
        var bGet = b.get(random);
        if (bGet == null) { return null; }

        return merger.apply(aGet, bGet);
    }

    /**
     * The number of options in the table
     * If the two sub-tables both have a finite length, the result of this method
     * is the product of both of them. If either or both have an infinite length, the result is Optional.empty.
     * @return The number of options in this table.
     */
    @Override
    public OptionalInt length() {
        var left = a.length();
        var right = b.length();

        if (left.isPresent() && right.isPresent()) {
            return OptionalInt.of(left.getAsInt() * right.getAsInt());
        } else {
            return OptionalInt.empty();
        }
    }
}
