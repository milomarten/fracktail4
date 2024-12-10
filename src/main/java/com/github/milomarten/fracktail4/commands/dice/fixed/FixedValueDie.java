package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.RollResult;
import com.github.milomarten.fracktail4.commands.dice.Rollable;
import org.apache.commons.rng.UniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;

public class FixedValueDie<E extends Enum<E> & FixedValue> implements Rollable<E> {
    /**
     * The source of randomness for the dice rolls.
     * By default, uses a new instance of java.util.Random.
     */
    private final UniformRandomProvider randomSource;

    private final E[] options;

    public FixedValueDie(Class<E> clazz) {
        this(clazz, RandomSource.MT.create());
    }

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
