package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.fixed.FixedValue;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import org.apache.commons.collections4.IteratorUtils;

import java.util.Iterator;
import java.util.List;

public class MockFixedDie<E extends Enum<E> & FixedValue> implements Rollable<E> {
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
    public RollResult<E> roll() {
        E roll = this.values.next();
        return new RollResult<>(roll, roll.getStatus());
    }
}
