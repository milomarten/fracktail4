package com.github.milomarten.fracktail5.platform;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * A visitor which collects multiple visitors together
 * Most apps that use Visitors accept multiple, so this allows this common behavior to be
 * used across all classes that use Visitors.
 * @param <T> The input and output type
 */
@NoArgsConstructor
public class VisitorGroup<T> implements Visitor<T> {
    private final List<Visitor<T>> visitors = new ArrayList<>();

    public VisitorGroup(VisitorGroup<T> original) {
        this.visitors.addAll(original.visitors);
    }

    public VisitorGroup(List<Visitor<T>> original) {
        this.visitors.addAll(original);
    }

    public VisitorGroup<T> add(Visitor<T> visitor) {
        visitors.add(visitor);
        return this;
    }

    @Override
    public T visit(T input) {
        var output = input;
        for (var visitor : visitors) {
            output = visitor.visit(output);
        }
        return output;
    }
}
