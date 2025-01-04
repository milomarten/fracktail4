package com.github.milomarten.fracktail4.commands.dice;

import org.apache.commons.rng.UniformRandomProvider;

public interface Rollable<T> {
    RollResult<T> roll(UniformRandomProvider random);
    default void validate() {}
}
