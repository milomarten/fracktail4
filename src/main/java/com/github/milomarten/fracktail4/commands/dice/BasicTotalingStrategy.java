package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;

import java.util.List;
import java.util.stream.Collectors;

public class BasicTotalingStrategy<T> implements DiceTotalingStrategy<T> {
    @Override
    public TermEvaluationResult compile(List<AbstractDiceExpression.Result<T>> rolls, DiceEvaluatorOptions options) {
        var string = rolls.stream()
                .map(r -> r.toString(options.getOutputType()))
                .collect(Collectors.joining(" "));

        return new TermEvaluationResult(null, string);
    }
}
