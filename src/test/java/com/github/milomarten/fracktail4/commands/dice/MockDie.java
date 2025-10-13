package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail.core.dice.RollResult;
import com.github.milomarten.fracktail.core.dice.die.Die;
import com.github.milomarten.fracktail.core.dice.term.Status;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.Iterator;
import java.util.List;

public class MockDie extends Die {
    private Iterator<Integer> values;

    public MockDie(int numSides, int... rollValues) {
        super(numSides);
        setRolls(rollValues);
    }

    public void setRolls(int... rollValues) {
        int lastValue = rollValues[rollValues.length - 1];
        this.values = IteratorUtils.chainedIterator(
                IteratorUtils.arrayIterator(rollValues),
                IteratorUtils.loopingIterator(List.of(lastValue)));
    }

    @Override
    public RollResult<Integer> roll(UniformRandomProvider random) {
        int roll = this.values.next();
        if (roll == 1) { return new RollResult<>(roll, Status.CRITICAL_FAIL); }
        else if (roll == this.getNumFaces()) { return new RollResult<>(roll, Status.CRITICAL_SUCCESS); }
        else { return new RollResult<>(roll); }
    }
}
