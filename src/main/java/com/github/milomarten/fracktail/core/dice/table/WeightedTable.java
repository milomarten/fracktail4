package com.github.milomarten.fracktail.core.dice.table;

import lombok.Getter;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A table with weighted entries that can be randomly selected from.
 * Each entry on the table has an associated weight, which describes how likely that
 * entry is to be randomly picked. If all weights are the same, each element is equally
 * likely to be picked. The exact odds is equal to [item weight] / [sum of the weight of all items]
 * <br>
 * Contents of the table can, in turn, be instances of RandomlySelected, allowing for nested tables.
 * <br>
 * Each item on the table must be unique, as determined by equals(). If a duplicate entry is added, the weights are
 * combined, and the table size remains the same. This check only compares siblings, and isn't recursive; as such,
 * an individual table entry can be within this table as well as a subtable, or within two subtables. However
 * two identical subtables (as determined by equals) is NOT permitted.
 * <br>
 * Weights of 0 are permitted, and function essentially as being disabled. Negative weights are not allowed, and will
 * fail with IllegalArgumentException.
 * @param <T> The contents of the table.
 */
public class WeightedTable<T> implements RandomlySelected<T> {
    @Getter private final List<Entry<T>> table;
    @Getter private int totalWeightSize = 0;

    // For speed, rather than looping through the table every time, we use a TreeMap for 0(1) response.
    // The creation of the TreeMap happens lazily, only when get(Random) is called
    private boolean needsRecompile = false;
    private final TreeMap<Integer, RandomlySelected<T>> tableProcessed = new TreeMap<>();

    public WeightedTable(List<Entry<T>> table) {
        this.table = new ArrayList<>(table);
        this.needsRecompile = !table.isEmpty();
    }

    public WeightedTable() {
        this.table = new ArrayList<>();
    }

    private void assertNonNegative(int number, boolean allowZero) {
        if (number < 0) {
            throw new IllegalArgumentException(number + " must be non-negative");
        } else if (number == 0 && !allowZero) {
            throw new IllegalArgumentException(number + " must be positive");
        }
    }

    private void onWrite() {
        needsRecompile = true;
    }

    /**
     * Check if the entry exists, as determined by equals
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param entry The entry to check
     * @return True if it exists
     */
    public boolean exists(RandomlySelected<T> entry) {
        return table.stream()
                .anyMatch(we -> we.content.equals(entry));
    }

    /**
     * Check if the entry exists, as determined by equals.
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param entry The entry to check
     * @return True if it exists
     */
    public boolean exists(T entry) {
        return exists(new Static<>(entry));
    }

    /**
     * Add an entry to this table
     * @param weight The weight to use
     * @param entry The entry to add
     * @throws IllegalArgumentException This entry already exists
     */
    public void add(int weight, RandomlySelected<T> entry) {
        assertNonNegative(weight, true);
        if (exists(entry)) {
            throw new IllegalArgumentException("Entry " + entry + " already exists");
        }
        table.add(new Entry<>(weight, entry));
        onWrite();
    }

    /**
     * Add an entry to this table
     * @param weight The weight to use
     * @param entry The entry to add
     * @throws IllegalArgumentException This entry already exists
     */
    public void add(int weight, T entry) {
        add(weight, new RandomlySelected.Static<>(entry));
    }

    /**
     * Remove an entry from this table
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param entry The entry to remove
     * @return True if the entry was actually there
     */
    public boolean remove(RandomlySelected<T> entry) {
        var modified = table.removeIf(we -> we.content.equals(entry));
        if (modified) {
            onWrite();
        }
        return modified;
    }

    /**
     * Remove an entry from this table
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param entry The entry to remove
     * @return True if the entry was actually there
     */
    public boolean remove(T entry) {
        return remove(new RandomlySelected.Static<>(entry));
    }

    /**
     * Adjust the weight of an entry
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param newWeight The new weight to use
     * @param entry The entry to adjust
     * @throws IllegalArgumentException The entry to reweigh doesn't exist
     */
    public void reweigh(int newWeight, RandomlySelected<T> entry) {
        assertNonNegative(newWeight, true);
        var removed = remove(entry);
        if (removed) {
            table.add(new Entry<>(newWeight, entry));
        } else {
            throw new IllegalArgumentException("Entry " + entry + " does not exist");
        }
    }

    /**
     * Adjust the weight of an entry
     * Note that this entry only checks this table's entries, it does not recurse into any subtables.
     * @param newWeight The new weight to use
     * @param entry The entry to adjust
     * @throws IllegalArgumentException The entry to reweigh doesn't exist
     */
    public void reweigh(int newWeight, T entry) {
        reweigh(newWeight, new Static<>(entry));
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
        needsRecompile = false;
    }

    @Override
    public T get(UniformRandomProvider random) {
        if (needsRecompile) {
            recompile();
        }

        if (totalWeightSize == 0) {
            return null; // no meaningful entries present...
        }

        var roll = random.nextInt(totalWeightSize);
        return tableProcessed.floorEntry(roll).getValue().get(random);
    }

    /**
     * A entry with some weight
     * @param weight The weight of the entry
     * @param content The content itself, allowing for recursion.
     * @param <T> The type of the contained content.
     */
    public record Entry<T>(int weight, RandomlySelected<T> content) {}
}
