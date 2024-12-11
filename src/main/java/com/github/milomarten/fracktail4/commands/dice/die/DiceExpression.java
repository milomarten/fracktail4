package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.*;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.github.milomarten.fracktail4.commands.dice.Utils.*;

/**
 * Represents an expression for how to roll, reroll, discard, and total dice.
 * Order of operations is:
 * 1. Roll the number of dice
 * 2. Do all rerolls, if specified
 * 3. Do all explodes, if specified
 * 4. Drop lowest dice, if specified
 * 5. Keep highest/lowest dice, if specified
 * 6. Add results depending on totaling strategy
 * For safety, you cannot roll more than 64 dice at once, and the max number of faces is 1000.
 * All other parameters are fine, although must be positive.
 * number of dice can be negative, although
 * this solely indicates that the final result should be negative. For example, -3d4 will
 * roll three dice, and negate the final result.
 */
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = false)
public class DiceExpression extends AbstractDiceExpression<Integer> {
    /**
     * The number of dice to roll. Default = 1
     */
    @Builder.Default int numberOfDice = 1;
    /**
     * The dice (or non-dice!) to roll.
     */
    Rollable<Integer> die;
    /**
     * The number of dice to drop. The n lowest dice will be discarded.
     * By default, numberToDrop is 0, so no dice are dropped.
     */
    @Builder.Default int numberToDrop = 0;
    /**
     * The number of dice to keep.
     * The highest (or lowest) n dice will be kept, and all others discarded.
     * Use the `keepLowest` flag to determine if the lowest or highest are kept.
     * By default, numberToKeep is `Integer.MAX_VALUE`, so all dice are kept.
     */
    @Builder.Default int numberToKeep = Integer.MAX_VALUE;
    /**
     * Whether the lowest or highest dice are kept.
     * By default, keepLowest is false, meaning the highest n dice are kept.
     */
    @Builder.Default boolean keepLowest = false;
    /**
     * Dice below this value are discarded and rerolled.
     * By default, rerollAt is `Integer.MIN_VALUE`, so no dice are rerolled.
     */
    @Builder.Default int rerollAt = -1;
    /**
     * For all dice above this value, more dice are rolled.
     * By default, explodeAt is `Integer.MAX_VALUE`, so no dice are exploded.
     */
    @Builder.Default int explodeAt = Integer.MAX_VALUE;
    /**
     * Describe how the roll results should be interpreted.
     * By default, this is `SumDiceStrategy.INSTANCE`, which simply adds the face value
     * of all dice, discarding the marked dice appropriately.
     */
    @Builder.Default DiceTotalingStrategy<Integer> totalingStrategy = SumDiceStrategy.INSTANCE;

    @Override
    public Term drop(Term qty, DiceEvaluatorOptions options) {
        var qtyEval = qty.evaluate(options);
        this.setNumberToDrop(qtyEval.valueAsInt(options.getRoundingMode()));
        return this;
    }

    @Override
    public Term keep(Term qty, DiceEvaluatorOptions options) {
        var keepEval = qty.evaluate(options);
        this.setNumberToKeep(keepEval.valueAsInt(options.getRoundingMode()));
        this.setKeepLowest(false);
        return this;
    }

    @Override
    public Term keepLow(Term qty, DiceEvaluatorOptions options) {
        var keepEval = qty.evaluate(options);
        this.setNumberToKeep(keepEval.valueAsInt(options.getRoundingMode()));
        this.setKeepLowest(true);
        return this;
    }

    @Override
    public Term reroll(Term at, boolean infinite, DiceEvaluatorOptions options) {
        var rerollEval = at.evaluate(options);
        this.setRerollAt(rerollEval.valueAsInt(options.getRoundingMode()));
        this.setInfiniteReroll(infinite);
        return this;
    }

    @Override
    public Term explode(Term at, boolean infinite, DiceEvaluatorOptions options) {
        var explodeEval = at.evaluate(options);
        this.setExplodeAt(explodeEval.valueAsInt(options.getRoundingMode()));
        this.setInfiniteExplode(infinite);
        return this;
    }

    @Override
    public Term success(Term at, DiceEvaluatorOptions options) {
        if (totalingStrategy instanceof SuccessFailureStrategy<Integer> sfs) {
            sfs.setSuccessThreshold(at.evaluate(options).valueAsInt(options.getRoundingMode()));
        } else {
            var sfs = new SuccessFailureStrategy<>(0, Integer.MAX_VALUE);
            sfs.setSuccessThreshold(at.evaluate(options).valueAsInt(options.getRoundingMode()));
            this.totalingStrategy = sfs;
        }
        return this;
    }

    @Override
    public Term failure(Term at, DiceEvaluatorOptions options) {
        if (totalingStrategy instanceof SuccessFailureStrategy<Integer> sfs) {
            sfs.setFailureThreshold(at.evaluate(options).valueAsInt(options.getRoundingMode()));
        } else {
            var sfs = new SuccessFailureStrategy<>(0, Integer.MAX_VALUE);
            sfs.setFailureThreshold(at.evaluate(options).valueAsInt(options.getRoundingMode()));
            this.totalingStrategy = sfs;
        }
        return this;
    }

    @Override
    protected void validate() {
        checkRange(Math.abs(numberOfDice),0, 32, "Number Of Tokens");
        checkPositive(numberToDrop, "Number to Drop");
        checkPositive(numberToKeep, "Number to Keep");
        this.die.validate();
        this.totalingStrategy.validate();
    }

    @Override
    protected List<RollResult<Integer>> initialRoll() {
        return Utils.doNTimes(Math.abs(this.numberOfDice), this::roll);
    }

    @Override
    protected RollResult<Integer> roll() {
        return die.roll();
    }

    @Override
    protected boolean shouldDiscountAndReroll(RollResult<Integer> result) {
        return result.getValue() <= this.rerollAt;
    }

    @Override
    protected int shouldExplodeInto(RollResult<Integer> result) {
        return result.getValue() >= this.explodeAt ? 1 : 0;
    }

    @Override
    protected void dropDice(List<Result<Integer>> rolls) {
        // Handle Drops
        dropLowestDice(rolls, this.numberToDrop);

        // Handle Keeps - Which is just drops, really
        var numNonDiscountedRolls = (int)rolls.stream().filter(r -> !r.isDiscounted()).count();
        if (numNonDiscountedRolls > numberToKeep) {
            var newNumberToDrop = numNonDiscountedRolls - numberToKeep;
            if (keepLowest) {
                dropHighestDice(rolls, newNumberToDrop);
            }
            else {
                dropLowestDice(rolls, newNumberToDrop);
            }
        }
    }

    @Override
    protected TermEvaluationResult compileResults(List<Result<Integer>> rolls, DiceEvaluatorOptions options) {
        var finalResults = totalingStrategy.compile(rolls, options);
        if (Math.signum(this.numberOfDice) < 0) {
            finalResults = finalResults.map(BigDecimal::negate, s -> "-" + s);
        }
        return finalResults;
    }

    public void dropLowestDice(List<Result<Integer>> rolls, int n) {
        if (n == 0) return;
        rolls.stream()
            .sorted(Comparator.comparing(r -> r.getRoll().getValue()))
            .limit(n)
            .forEach(Result::discount);
    }

    public void dropHighestDice(List<Result<Integer>> rolls, int n) {
        if (n == 0) return;
        rolls.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(r -> r.getRoll().getValue())))
                .limit(n)
                .forEach(Result::discount);
    }
}
