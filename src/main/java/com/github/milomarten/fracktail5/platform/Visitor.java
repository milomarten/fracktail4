package com.github.milomarten.fracktail5.platform;

public interface Visitor<T> {
    T visit(T input);
}
