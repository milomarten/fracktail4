package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.rng.UniformRandomProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

@Getter
@Setter
@SuperBuilder
public abstract class AbstractDiceExpression<T> implements Term {
    /**
     * Controls whether rerolls should occur "infinitely" or not.
     * If true, rerolls will continue to happen until a ceiling of 100 tries.
     * If false, rerolls will only happen once.
     * By default, infiniteReroll is false, so dice are rerolled only once.
     */
    @Builder.Default boolean infiniteReroll = false;
    /**
     * Controls whether explodes should occur "infinitely" or not.
     * If true, explodes will continue to happen until a ceiling of 100 tries.
     * If false, explodes will only happen once.
     * By default, infiniteExplode is false, so explosions only happen once.
     */
    @Builder.Default boolean infiniteExplode = false;

    @Override
    public final TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var rolls = initialRoll(options.getRandom()).stream()
                .map(r -> new Result<>(r, false, false))
                .collect(Collectors.toCollection(ArrayList::new));

        // Reroll any dice as per the template's logic
        for (int i = 0; i < 100; i++) {
            var numRerolls = rolls.stream()
                    .filter(r -> !r.dropped)
                    .filter(r -> {
                        if (shouldDiscountAndReroll(r.roll)) {
                            r.dropped = true; // Extremely illegal but it's ok.
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .count();
            LongStream.range(0, numRerolls)
                    .mapToObj(j -> roll(options.getRandom()))
                    .map(r -> new Result<>(r, false, false))
                    .forEach(rolls::add);
            if (!infiniteReroll || numRerolls == 0) {
                break;
            }
        }

        // Explode any dice as per the template's logic
        for (int i = 0; i < 100; i++) {
            var numExplosions = rolls.stream()
                    .filter(r -> !r.dropped && !r.exploded)
                    .mapToInt(r -> {
                        var num = shouldExplodeInto(r.roll);
                        if (num > 0) {
                            r.exploded = true;
                        }
                        return num;
                    })
                    .sum();
            LongStream.range(0, numExplosions)
                    .mapToObj(j -> roll(options.getRandom()))
                    .map(r -> new Result<>(r, false, false))
                    .forEach(rolls::add);
            if (!infiniteExplode || numExplosions == 0) {
                break;
            }
        }

        dropDice(rolls);
        return compileResults(rolls, options);
    }

    protected abstract List<RollResult<T>> initialRoll(UniformRandomProvider random);

    protected abstract RollResult<T> roll(UniformRandomProvider random);

    protected abstract boolean shouldDiscountAndReroll(RollResult<T> result);

    protected abstract int shouldExplodeInto(RollResult<T> result);

    protected abstract void dropDice(List<Result<T>> rolls);

    protected abstract TermEvaluationResult compileResults(List<Result<T>> rolls, DiceEvaluatorOptions options);

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter @ToString
    public static class Result<T> {
        private final RollResult<T> roll;
        private boolean dropped;
        private boolean exploded;

        public void drop() {
            this.dropped = true;
        }

        public String toString() {
            var str = String.valueOf(roll.getValue());
            return dropped ? "~~" + str + "~~" : str;
        }
    }
}
