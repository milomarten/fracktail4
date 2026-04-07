package com.github.milomarten.fracktail.core.dice.table;

import org.apache.commons.rng.core.source32.MersenneTwister;
import org.apache.commons.rng.simple.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static org.junit.jupiter.api.Assertions.*;

class WeightedTableTest {
    @Test
    public void simpleTableEqualWeights() {
        var table = new WeightedTable<Boolean>();
        table.add(1, true);
        table.add(1, false);

        var results = monteCarlo(table);
        assertWithinError(results.get(true), 0.5, 0.01);
        assertWithinError(results.get(false), 0.5, 0.01);
    }

    @Test
    public void tableUnequalWeights() {
        var table = new WeightedTable<Boolean>();
        table.add(3, true);
        table.add(1, false);

        var results = monteCarlo(table);
        assertWithinError(results.get(true), 0.75, 0.01);
        assertWithinError(results.get(false), 0.25, 0.01);
    }

    @Test
    public void tableUnequalAwkwardWeights() {
        var table = new WeightedTable<Boolean>();
        table.add(2, true);
        table.add(1, false);

        var results = monteCarlo(table);
        assertWithinError(results.get(true), 0.666, 0.01);
        assertWithinError(results.get(false), 0.333, 0.01);
    }

    private static <T> Map<T, Double> monteCarlo(WeightedTable<T> table) {
        var random = RandomSource.KISS.create();
        var collector = new HashMap<T, LongAdder>();
        for (int i = 0; i < 10_000; i++) {
            var roll = table.get(random);
            collector.computeIfAbsent(roll, l -> new LongAdder()).add(1);
        }

        var normalizer = new HashMap<T, Double>();
        collector.forEach((item, count) -> {
            normalizer.put(item, count.doubleValue() / 10_000);
        });
        return normalizer;
    }

    private static void assertWithinError(double check, double expect, double delta) {
        assertTrue(check >= expect - delta && check <= expect + delta,
                String.format("Expected %f to be %f ± %f", check, expect, delta));
    }
}