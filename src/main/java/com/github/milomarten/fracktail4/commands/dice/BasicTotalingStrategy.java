package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The most generic "totaling" strategy, which just prints each roll with no other effect.
 * The TermEvaluationResult here is empty, so direct attempts to resolve as a number (for instance,
 * doing math) will fail.
 * @param <T> The type contained in the rolls.
 */
public class BasicTotalingStrategy<T> implements DiceTotalingStrategy<T> {
    @Override
    public TermEvaluationResult compile(List<AbstractDiceExpression.Result<T>> rolls, DiceEvaluatorOptions options) {
        var string = rolls.stream()
                .map(AbstractDiceExpression.Result::toString)
                .collect(Collectors.joining(" "));

        return new TermEvaluationResult(null, string);
    }
}
