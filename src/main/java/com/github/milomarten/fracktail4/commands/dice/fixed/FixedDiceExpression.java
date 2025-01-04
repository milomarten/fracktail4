package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.*;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.List;
import java.util.OptionalInt;

import static com.github.milomarten.fracktail4.commands.dice.Utils.checkRange;
import static com.github.milomarten.fracktail4.commands.dice.Utils.doNTimes;

/**
 * A generic expression which supports a FixedValueDie
 * This is specifically catered to die, or other chance items, where a number is not involved.
 * As such, these types of expressions only allow you to specify the die, the number of die,
 * and the method of computing the final result.
 * By default, the totaling strategy is the BasicTotalingStrategy, which simply prints each result
 * separated by a space.
 * @param <E> The type that represents each face of the die.
 */
@SuperBuilder
public class FixedDiceExpression<E extends Enum<E> & DieValue> extends AbstractDiceExpression<E> {
    @Builder.Default int numberOfDice = 1;
    Rollable<E> die;
    @Builder.Default DiceTotalingStrategy<E> totalingStrategy = new BasicTotalingStrategy<>();

    @Override
    protected void validate() {
        super.validate();
        checkRange(Math.abs(numberOfDice),0, 32, "Number Of Dice");
        die.validate();
        totalingStrategy.validate();
    }

    @Override
    public OptionalInt getNumberOfDiceIfApplicable() {
        return OptionalInt.of(numberOfDice);
    }

    @Override
    protected List<RollResult<E>> initialRoll(UniformRandomProvider random) {
        return doNTimes(this.numberOfDice, () -> roll(random));
    }

    @Override
    protected RollResult<E> roll(UniformRandomProvider random) {
        return die.roll(random);
    }

    @Override
    protected boolean shouldDiscountAndReroll(RollResult<E> result) {
        return false;
    }

    @Override
    protected int shouldExplodeInto(RollResult<E> result) {
        return 0;
    }

    @Override
    protected void dropDice(List<Result<E>> rolls) {

    }

    @Override
    protected TermEvaluationResult compileResults(List<Result<E>> rolls, DiceEvaluatorOptions options) {
        return this.totalingStrategy.compile(rolls, options);
    }
}
