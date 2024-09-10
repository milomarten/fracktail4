package com.github.milomarten.fracktail4.base;

import lombok.Builder;
import lombok.Singular;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A typical use case, where invoking a command should return one or more random responses.
 * The randomness of the response can be weighted, so some responses are more likely than
 * others. Using a weight of NaN or an infinity will result in an exception. A negative or 0
 * value will not throw an exception, but that value will be ignored.
 */
public class RandomCommand extends SimpleNoParameterCommand {
    private final Random random = new Random();
    private final NavigableMap<Double, String> weights;
    private final double total;

    @Builder
    private RandomCommand(String name, String description, @Singular Map<String, Double> options) {
        super(name, description);
        if (options.isEmpty()) {
            throw new IllegalArgumentException("Options is empty");
        }

        this.weights = new TreeMap<>();
        var runningTotal = 0d;
        for (var option : options.keySet()) {
            var weight = options.get(option);
            if (weight == null || !Double.isFinite(weight)) {
                throw new IllegalArgumentException(option);
            } else if (weight <= 0) {
                continue;
            }
            runningTotal += weight;
            weights.put(runningTotal, option);
        }
        this.total = runningTotal;
    }

    /**
     * Factory method for the simple use case of equal randomness between options.
     * Each option will be assigned a weight of 1. Note that, if the same string is provided multiple
     * times, the weights are stacked: fromList(..., "a", "a", "b") results in an "a" more frequently
     * than fromList(..., "a", "b").
     * @param name The name of the command
     * @param description The command description
     * @param options The response options.
     * @throws IllegalArgumentException no options are present
     * @return A RandomCommand configured to serve the response options with equal likelihood.
     */
    public static RandomCommand fromList(String name, String description, String... options) {
        Map<String, Double> weights = Arrays.stream(options)
                .collect(Collectors.toMap(Function.identity(), s -> 1.0, Double::sum));
        return new RandomCommand(name, description, weights);
    }

    @Override
    public String getResponse() {
        double blob = random.nextDouble() * this.total;
        return this.weights.higherEntry(blob).getValue();
    }
}
