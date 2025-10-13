package com.github.milomarten.fracktail5.platform;

/**
 * A generic interface which performs some transformation on its input
 * This is used in a number of ways, usually as a way to wrap type T in some way.
 * However, there is no requirement that the input and output are related at all.
 * @param <T> The input and output type
 */
public interface Visitor<T> {
    /**
     * Perform a transformation on the input
     * @param input The input
     * @return The output
     */
    T visit(T input);
}
