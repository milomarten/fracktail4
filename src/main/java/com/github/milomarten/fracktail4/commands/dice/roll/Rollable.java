package com.github.milomarten.fracktail4.commands.dice.roll;

public interface Rollable<T> {
    RollResult<T> roll();
    default void validate() {}
}
