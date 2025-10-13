package com.github.milomarten.fracktail.core.dice;

import org.apache.commons.rng.UniformRandomProvider;

public interface Rollable<T> {
    RollResult<T> roll(UniformRandomProvider random);
    default void validate() {}
}
