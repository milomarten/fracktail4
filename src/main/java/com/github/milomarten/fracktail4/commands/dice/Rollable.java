package com.github.milomarten.fracktail4.commands.dice;

public interface Rollable<T> {
    RollResult<T> roll();
    default void validate() {}
}
