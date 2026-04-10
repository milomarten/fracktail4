package com.github.milomarten.fracktail.core.dice.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;

/**
 * A table where each entry is equally possible when randomly selected.
 * Equivalent to a WeightedTable where all weights are the same, this avoids some costly
 * computation and allows for simply picking randomly from a list.
 * <br>
 * Like WeightedTable, contents of the table can, in turn, be instances of RandomlySelected, allowing for nested tables.
 * @param <T> The contents of the table.
 */
public class UnweightedTable<T> implements RandomlySelected<T> {
    private final List<RandomlySelected<T>> table;

    public UnweightedTable(List<? extends RandomlySelected<T>> table) {
        this.table = new ArrayList<>(table);
    }

    public static <T> UnweightedTable<T> fromArray(T[] table) {
        return new UnweightedTable<>(Arrays.stream(table)
                .map(Static::new)
                .toList());
    }

    @Override
    public T get(UniformRandomProvider random) {
        if (table.isEmpty()) {
            return null;
        }
        return table.get(random.nextInt(table.size())).get(random);
    }

    @Override
    public OptionalInt length() {
        return OptionalInt.of(table.size());
    }
}
