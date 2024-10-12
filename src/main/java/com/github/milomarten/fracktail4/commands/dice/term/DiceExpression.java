package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.github.milomarten.fracktail4.commands.dice.Utils.checkPositive;
import static com.github.milomarten.fracktail4.commands.dice.Utils.checkRange;

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
@Builder
@Data
public class DiceExpression implements Term {
    /**
     * The number of dice to roll. Default = 1
     */
    @Builder.Default int numberOfDice = 1;
    /**
     * The number of sides on the dice to roll.
     */
    int numberOfSides;
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
     * Controls whether rerolls should occur "infinitely" or not.
     * If true, rerolls will continue to happen until a ceiling of 100 tries.
     * If false, rerolls will only happen once.
     * By default, infiniteReroll is false, so dice are rerolled only once.
     */
    @Builder.Default boolean infiniteReroll = false;
    /**
     * For all dice above this value, more dice are rolled.
     * By default, explodeAt is `Integer.MAX_VALUE`, so no dice are exploded.
     */
    @Builder.Default int explodeAt = Integer.MAX_VALUE;
    /**
     * Controls whether explodes should occur "infinitely" or not.
     * If true, explodes will continue to happen until a ceiling of 100 tries.
     * If false, explodes will only happen once.
     * By default, infiniteExplode is false, so explosions only happen once.
     */
    @Builder.Default boolean infiniteExplode = false;
    /**
     * Describe how the roll results should be interpreted.
     * By default, this is `SumDiceStrategy.INSTANCE`, which simply adds the face value
     * of all dice, discarding the marked dice appropriately.
     */
    @Builder.Default DiceTotalingStrategy totalingStrategy = SumDiceStrategy.INSTANCE;
    /**
     * The source of randomness for the dice rolls.
     * By default, uses a new instance of java.util.Random.
     */
    @Builder.Default RandomGenerator randomSource = new Random();

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        // Flip an error if this dice expression is too high.
        validate();

        var negateAtTheEnd = numberOfDice < 0;
        var normalizedNumberOfDice = Math.abs(numberOfDice);

        // 1. Roll Dice
        var results = new Results(doNTimes(normalizedNumberOfDice, this::roll));
        // a - Reroll low ones
        for (int i = 0; i < 100; i++) {
            var rerolls = results.discountLowerThan(this.rerollAt);
            doNTimes(rerolls, () -> results.addResult(roll()));
            if (!infiniteReroll || rerolls == 0) {
                break;
            }
        }

        // b - Explode high ones
        var previousExplodes = 0L;
        for (int i = 0; i < 100; i++) {
            var explodes = results.returnCountHigherThan(this.explodeAt) - previousExplodes;
            doNTimes(explodes, () -> results.addResult(roll()));
            if (!infiniteExplode || explodes == 0) {
                break;
            }
            previousExplodes += explodes;
        }

        // 2. Drop the number requested
        results.dropLowestDice(numberToDrop);
        // 3. Keep the number requested
        if (results.lengthNotDiscounted > numberToKeep) {
            int numDropFromKeep = results.lengthNotDiscounted - numberToKeep;
            if (keepLowest) {
                results.dropHighestDice(numDropFromKeep);
            } else {
                results.dropLowestDice(numDropFromKeep);
            }
        }

        var finalResults = totalingStrategy.compile(results, options);
        if (negateAtTheEnd) {
            finalResults = finalResults.map(BigDecimal::negate, s -> "-" + s);
        }
        return finalResults;
    }

    private Result roll() {
        if (numberOfSides == 0) { return new Result(0); }
        var roll = randomSource.nextInt(numberOfSides) + 1;
        var result = new Result(roll);
        if (roll == 1) {
            result.setStatus(Status.CRITICAL_FAIL);
        } else if (roll == numberOfSides) {
            result.setStatus(Status.CRITICAL_SUCCESS);
        }
        return result;
    }

    private static void doNTimes(long number, Runnable action) {
        if (number == 0) return;
        LongStream.range(0, number).forEach(i -> action.run());
    }

    private static <T> List<T> doNTimes(int number, Supplier<T> action) {
        if (number == 0) return List.of();
        return IntStream.range(0, number).mapToObj(i -> action.get()).toList();
    }

    private void validate() {
        checkRange(Math.abs(numberOfDice),0, 32, "Number Of Dice");
        checkRange(numberOfSides, 0, 1000, "Number of Sides");
        checkPositive(numberToDrop, "Number to Drop");
        checkPositive(numberToKeep, "Number to Keep");
        this.totalingStrategy.validate();
    }

    @Data
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Result {
        private final int value;
        private boolean discounted;
        private Status status = Status.NEUTRAL;

        public String toPlainString() {
            return String.valueOf(value);
        }

        public String toAnsiString() {
            return status.format(this.value);
        }

        public String toString(DiceEvaluatorOptions options) {
            return switch (options.getOutputType()) {
                case PLAIN -> this.toPlainString();
                case ANSI -> this.toAnsiString();
            };
        }
    }

    public static class Results {
        private final List<Result> results;
        private int lengthNotDiscounted;

        public Results(List<Result> results) {
            this.results = new ArrayList<>(results);
            this.lengthNotDiscounted = (int) results.stream()
                    .filter(r -> !r.discounted)
                    .count();
        }

        public Stream<Result> getAllResults() {
            return results.stream();
        }

        private Stream<Result> getNonDiscountedResults() {
            return results.stream()
                    .filter(r -> !r.discounted);
        }

        public void dropLowestDice(int n) {
            if (n == 0) return;
            getNonDiscountedResults()
                    .sorted(Comparator.comparing(Result::getValue))
                    .limit(n)
                    .forEach(r -> {
                        r.discounted = true;
                        this.lengthNotDiscounted--;
                    });
        }

        public void dropHighestDice(int n) {
            if (n == 0) return;
            getNonDiscountedResults()
                    .sorted(Comparator.comparing(Result::getValue).reversed())
                    .limit(n)
                    .forEach(r -> {
                        r.discounted = true;
                        this.lengthNotDiscounted--;
                    });
        }

        /**
         * Count every dice less than or equal to n. Also discounts them.
         * @param n The maximum roll to discount
         * @return The number found
         */
        public long discountLowerThan(int n) {
            var toReroll = getNonDiscountedResults()
                    .filter(r -> r.value <= n)
                    .toList();
            toReroll.forEach(r -> r.discounted = true);
            return toReroll.size();
        }

        /**
         * Count every dice greater than or equal to n.
         * @param n The minimum roll to count
         * @return The number found
         */
        public long returnCountHigherThan(int n) {
            return getNonDiscountedResults()
                    .filter(r -> r.value >= n)
                    .count();
        }

        /**
         * Add a new roll to this result
         * @param roll The roll to add
         */
        public void addResult(Result roll) {
            this.results.add(roll);
            this.lengthNotDiscounted++;
        }
    }

    @Override
    public Term drop(Term qty, DiceEvaluatorOptions options) {
        var qtyEval = qty.evaluate(options);
        this.setNumberToDrop(qtyEval.valueAsInt());
        return this;
    }

    @Override
    public Term keep(Term qty, DiceEvaluatorOptions options) {
        var keepEval = qty.evaluate(options);
        this.setNumberToKeep(keepEval.valueAsInt());
        this.setKeepLowest(false);
        return this;
    }

    @Override
    public Term keepLow(Term qty, DiceEvaluatorOptions options) {
        var keepEval = qty.evaluate(options);
        this.setNumberToKeep(keepEval.valueAsInt());
        this.setKeepLowest(true);
        return this;
    }

    @Override
    public Term reroll(Term at, boolean infinite, DiceEvaluatorOptions options) {
        var rerollEval = at.evaluate(options);
        this.setRerollAt(rerollEval.valueAsInt());
        this.setInfiniteReroll(infinite);
        return this;
    }

    @Override
    public Term explode(Term at, boolean infinite, DiceEvaluatorOptions options) {
        var explodeEval = at.evaluate(options);
        this.setExplodeAt(explodeEval.valueAsInt());
        this.setInfiniteExplode(infinite);
        return this;
    }

    @Override
    public Term success(Term at, DiceEvaluatorOptions options) {
        if (totalingStrategy instanceof SuccessFailureStrategy sfs) {
            sfs.setSuccessThreshold(at.evaluate(options).valueAsInt());
        } else {
            var sfs = new SuccessFailureStrategy();
            sfs.setSuccessThreshold(at.evaluate(options).valueAsInt());
            this.totalingStrategy = sfs;
        }
        return this;
    }

    @Override
    public Term failure(Term at, DiceEvaluatorOptions options) {
        if (totalingStrategy instanceof SuccessFailureStrategy sfs) {
            sfs.setFailureThreshold(at.evaluate(options).valueAsInt());
        } else {
            var sfs = new SuccessFailureStrategy();
            sfs.setFailureThreshold(at.evaluate(options).valueAsInt());
            this.totalingStrategy = sfs;
        }
        return this;
    }
}
