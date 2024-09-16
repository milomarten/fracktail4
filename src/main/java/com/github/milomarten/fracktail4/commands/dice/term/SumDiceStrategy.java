package com.github.milomarten.fracktail4.commands.dice.term;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * A simple strategy that sums the value of all dice faces, ignoring discounted dice.
 */
public enum SumDiceStrategy implements DiceTotalingStrategy {
    INSTANCE;

    @Override
    public TermEvaluationResult compile(DiceExpression.Results results) {
        var expr = new StringJoiner(" + ", "\uD83C\uDFB2(", ")");
        var sum = results.getAllResults()
                .<Integer>mapMulti((result, consumer) -> {
                    if (result.isDiscounted()) {
                        expr.add("~~" + result + "~~");
                    } else {
                        expr.add(result.toString());
                        consumer.accept(result.getValue());
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new TermEvaluationResult(BigDecimal.valueOf(sum), expr.toString());
    }
}
