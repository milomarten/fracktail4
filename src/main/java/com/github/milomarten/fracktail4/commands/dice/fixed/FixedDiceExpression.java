package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.*;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.List;

import static com.github.milomarten.fracktail4.commands.dice.Utils.checkRange;
import static com.github.milomarten.fracktail4.commands.dice.Utils.doNTimes;

@SuperBuilder
public class FixedDiceExpression<E extends Enum<E> & FixedValue> extends AbstractDiceExpression<E> {
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
    protected List<RollResult<E>> initialRoll() {
        return doNTimes(this.numberOfDice, this::roll);
    }

    @Override
    protected RollResult<E> roll() {
        return die.roll();
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
