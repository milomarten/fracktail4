package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceExpressionEvaluation;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * A simple strategy that sums the value of all dice faces, ignoring discounted dice.
 */
public enum SumDiceStrategy implements DiceTotalingStrategy {
    INSTANCE;

    @Override
    public DiceExpressionEvaluation compile(DiceExpression.Results results) {
        var expr = new StringJoiner(" + ", "(", ")");
        var sum = results.getAllResults()
                .<Integer>mapMulti((result, consumer) -> {
                    if (result.isDiscounted()) {
                        expr.add("~~" + result.getValue() + "~~");
                    } else {
                        expr.add("" + result.getValue());
                        consumer.accept(result.getValue());
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new DiceExpressionEvaluation(BigDecimal.valueOf(sum), expr.toString());
    }
}
