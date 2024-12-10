package com.github.milomarten.fracktail4.commands.dice.roll;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.*;
import lombok.experimental.SuperBuilder;

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
        validate();

        var rolls = initialRoll().stream()
                .map(r -> new Result<>(r, false, false))
                .collect(Collectors.toCollection(ArrayList::new));

        // Reroll any dice as per the template's logic
        for (int i = 0; i < 100; i++) {
            var numRerolls = rolls.stream()
                    .filter(r -> !r.discounted)
                    .filter(r -> {
                        if (shouldDiscountAndReroll(r.roll)) {
                            r.discounted = true; // Extremely illegal but it's ok.
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .count();
            LongStream.range(0, numRerolls)
                    .mapToObj(j -> roll())
                    .map(r -> new Result<>(r, false, false))
                    .forEach(rolls::add);
            if (!infiniteReroll || numRerolls == 0) {
                break;
            }
        }

        // Explode any dice as per the template's logic
        for (int i = 0; i < 100; i++) {
            var numExplosions = rolls.stream()
                    .filter(r -> !r.discounted && !r.exploded)
                    .mapToInt(r -> {
                        var num = shouldExplodeInto(r.roll);
                        if (num > 0) {
                            r.exploded = true;
                        }
                        return num;
                    })
                    .sum();
            LongStream.range(0, numExplosions)
                    .mapToObj(j -> roll())
                    .map(r -> new Result<>(r, false, false))
                    .forEach(rolls::add);
            if (!infiniteExplode || numExplosions == 0) {
                break;
            }
        }

        dropDice(rolls);
        return compileResults(rolls, options);
    }

    protected void validate() {}

    protected abstract List<RollResult<T>> initialRoll();

    protected abstract RollResult<T> roll();

    protected abstract boolean shouldDiscountAndReroll(RollResult<T> result);

    protected abstract int shouldExplodeInto(RollResult<T> result);

    protected abstract void dropDice(List<Result<T>> rolls);

    protected abstract TermEvaluationResult compileResults(List<Result<T>> rolls, DiceEvaluatorOptions options);

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Getter @ToString
    public static class Result<T> {
        private final RollResult<T> roll;
        private boolean discounted;
        private boolean exploded;

        public void discount() {
            this.discounted = true;
        }

        public String toPlainString() {
            var str = String.valueOf(roll.getValue());
            return discounted ? "~~" + str + "~~" : str;
        }

        public String toAnsiString() {
            return discounted ?
                    roll.getStatus().formatDiscounted(this.roll.getValue()) :
                    roll.getStatus().format(this.roll.getValue());
        }

        public String toString(DiceEvaluatorOptions.OutputType options) {
            return switch (options) {
                case PLAIN -> this.toPlainString();
                case ANSI -> this.toAnsiString();
            };
        }
    }
}
