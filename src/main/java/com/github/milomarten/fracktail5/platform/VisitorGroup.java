package com.github.milomarten.fracktail5.platform;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class VisitorGroup<T> implements Visitor<T> {
    private final List<Visitor<T>> visitors = new ArrayList<>();

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
