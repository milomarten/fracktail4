package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.Utils;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * A simple strategy that sums the value of all dice faces, ignoring discounted dice.
 */
public enum SumDiceStrategy implements DiceTotalingStrategy {
    INSTANCE;

    @Override
    public TermEvaluationResult compile(DiceExpression.Results results, DiceEvaluatorOptions options) {
        var expr = new StringJoiner(" + ", "\uD83C\uDFB2(", ")");
        var sum = results.getAllResults()
                .<Integer>mapMulti((result, consumer) -> {
                    String rollText = result.toString(options);
                    if (result.isDiscounted()) {
                        expr.add("~~" + rollText + "~~");
                    } else {
                        expr.add(rollText);
                        consumer.accept(result.getValue());
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new TermEvaluationResult(BigDecimal.valueOf(sum), expr.toString());
    }
}
