package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.fixed.DieValue;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.Iterator;
import java.util.List;

public class MockFixedDie<E extends Enum<E> & DieValue> implements Rollable<E> {
    private Iterator<E> values;

    public MockFixedDie(E... rollValues) {
        setRolls(rollValues);
    }

    public void setRolls(E... rollValues) {
        E lastValue = rollValues[rollValues.length - 1];
        this.values = IteratorUtils.chainedIterator(
                IteratorUtils.arrayIterator(rollValues),
                IteratorUtils.loopingIterator(List.of(lastValue)));
    }

    @Override
    public RollResult<E> roll(UniformRandomProvider random) {
        E roll = this.values.next();
        return new RollResult<>(roll, roll.getStatus());
    }
}
