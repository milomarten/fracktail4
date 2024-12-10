package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.Status;
import com.github.milomarten.fracktail4.commands.dice.roll.RollResult;
import com.github.milomarten.fracktail4.commands.dice.roll.Rollable;
import org.apache.commons.collections4.IteratorUtils;

import java.util.Iterator;
import java.util.List;

public class MockDie implements Rollable<Integer> {
    private int numSides;
    private Iterator<Integer> values;

    public MockDie(int numSides, int... rollValues) {
        this.numSides = numSides;
        setRolls(rollValues);
    }

    public void setRolls(int... rollValues) {
        int lastValue = rollValues[rollValues.length - 1];
        this.values = IteratorUtils.chainedIterator(
                IteratorUtils.arrayIterator(rollValues),
                IteratorUtils.loopingIterator(List.of(lastValue)));
    }

    @Override
    public RollResult<Integer> roll() {
        int roll = this.values.next();
        if (roll == 1) { return new RollResult<>(roll, Status.CRITICAL_FAIL); }
        else if (roll == numSides) { return new RollResult<>(roll, Status.CRITICAL_SUCCESS); }
        else { return new RollResult<>(roll); }
    }
}
