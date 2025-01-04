package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import org.apache.commons.rng.UniformRandomProvider;

/**
 * Represents a generic die which have an equal chance of rolling options presented by an enum
 * @param <E> The type that represents all options the face can be.
 */
public class FixedValueDie<E extends Enum<E> & DieValue> implements Rollable<E> {
    private final E[] options;

    /**
     * Create a die which uses the provided enum class, with specific randomness
     *
     * @param clazz The enum class
     */
    public FixedValueDie(Class<E> clazz) {
        this.options = clazz.getEnumConstants();
    }

    @Override
    public RollResult<E> roll(UniformRandomProvider random) {
        var roll = this.options[random.nextInt(this.options.length)];
        return new RollResult<>(roll, roll.getStatus());
    }
}
