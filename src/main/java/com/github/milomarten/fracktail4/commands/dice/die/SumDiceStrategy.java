package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceTotalingStrategy;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

/**
 * A simple strategy that sums the value of all dice faces, ignoring discounted dice.
 */
public enum SumDiceStrategy implements DiceTotalingStrategy<Integer> {
    INSTANCE;

    @Override
    public TermEvaluationResult compile(List<DiceExpression.Result<Integer>> results, DiceEvaluatorOptions options) {
        var expr = new StringJoiner(" + ", "\uD83C\uDFB2(", ")");
        var sum = results.stream()
                .<Integer>mapMulti((result, consumer) -> {
                    String rollText = result.toString(options.getOutputType());
                    if (result.isDiscounted()) {
                        expr.add(rollText);
                    } else {
                        expr.add(rollText);
                        consumer.accept(result.getRoll().getValue());
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new TermEvaluationResult(BigDecimal.valueOf(sum), expr.toString());
    }
}
