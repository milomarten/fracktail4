package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

/**
 * Represents a generic die which have an equal chance of rolling options presented by an enum
 * @param <E> The type that represents all options the face can be.
 */
public class FixedValueDie<E extends Enum<E> & DieValue> implements Rollable<E> {
    /**
     * The source of randomness for the dice rolls.
     * By default, uses Apache Commons Mersenne Twister
     */
    private final UniformRandomProvider randomSource;

    private final E[] options;

    /**
     * Create a die which uses the provided enum class, with default randomness
     * @param clazz The enum class
     */
    public FixedValueDie(Class<E> clazz) {
        this(clazz, RandomSource.MT.create());
    }

    /**
     * Create a die which uses the provided enum class, with specific randomness
     * @param clazz The enum class
     */
    public FixedValueDie(Class<E> clazz, UniformRandomProvider randomness) {
        this.options = clazz.getEnumConstants();
        this.randomSource = randomness;
    }

    @Override
    public RollResult<E> roll() {
        var roll = this.options[randomSource.nextInt(this.options.length)];
        return new RollResult<>(roll, roll.getStatus());
    }
}
