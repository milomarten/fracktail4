package com.github.milomarten.fracktail.core.dice.table;

import lombok.Getter;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.*;

/**
 * A table with weighted entries that can be randomly selected from.
 * Each entry on the table has an associated weight, which describes how likely that
 * entry is to be randomly picked. The exact odds is equal to [item weight] / [sum of the weight of all items].
 * If all weights are the same, consider using an UnweightedTable for a slight boost in efficiency.
 * <br>
 * Contents of the table can, in turn, be instances of RandomlySelected, allowing for nested tables.
 * <br>
 * Weights of 0 are permitted, and function essentially as being disabled. Negative weights are not allowed, and will
 * fail with IllegalArgumentException.
 * @param <T> The contents of the table.
 */
public class WeightedTable<T> implements RandomlySelected<T> {
    @Getter private final List<Entry<T>> table;
    @Getter private int totalWeightSize = 0;

    private final TreeMap<Integer, RandomlySelected<T>> tableProcessed = new TreeMap<>();

    public WeightedTable(List<Entry<T>> table) {
        this.table = new ArrayList<>(table);
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    void recompile() {
        totalWeightSize = 0;
        tableProcessed.clear();
        for (var entry : table) {
            if (entry.weight > 0) {
                tableProcessed.put(totalWeightSize, entry.content);
                totalWeightSize += entry.weight;
            }
        }
    }

    @Override
    public T get(UniformRandomProvider random) {
        if (tableProcessed.isEmpty()) {
            recompile();
        }

        if (totalWeightSize == 0) {
            return null; // no meaningful entries present...
        }

        var roll = random.nextInt(totalWeightSize);
        return tableProcessed.floorEntry(roll).getValue().get(random);
    }

    /**
     * The number of options in the table
     * This length counts the number of entries in this weighted table, discounting any weights set to 0,
     * since these functionally do not exist.
     * @return The number of possible options.
     */
    @Override
    public OptionalInt length() {
        if (tableProcessed.isEmpty()) {
            recompile();
        }

        return OptionalInt.of(totalWeightSize);
    }

    /**
     * A entry with some weight
     * @param weight The weight of the entry
     * @param content The content itself, allowing for recursion.
     * @param <T> The type of the contained content.
     */
    public record Entry<T>(int weight, RandomlySelected<T> content) {}

    public static class Builder<T> {
        private final List<Entry<T>> table = new ArrayList<>();

        public Builder<T> add(int weight, T content) {
            this.table.add(new Entry<>(weight, new RandomlySelected.Static<>(content)));
            return this;
        }

        public Builder<T> add(int weight, RandomlySelected<T> content) {
            this.table.add(new Entry<>(weight, content));
            return this;
        }

        public WeightedTable<T> build() {
            return new WeightedTable<>(table);
        }
    }
}
