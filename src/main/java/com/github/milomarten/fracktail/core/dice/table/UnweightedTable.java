package com.github.milomarten.fracktail.core.dice.table;

import org.apache.commons.rng.UniformRandomProvider;

import java.util.ArrayList;
import java.util.List;

public class UnweightedTable<T> implements RandomlySelected<T> {
    private final List<T> table;

    public UnweightedTable(List<T> table) {
        this.table = new ArrayList<>(table);
    }

    public UnweightedTable() {
        this.table = new ArrayList<>();
    }

    @Override
    public T get(UniformRandomProvider random) {
        if (table.isEmpty()) {
            return null;
        }
        return table.get(random.nextInt(table.size()));
    }
}
