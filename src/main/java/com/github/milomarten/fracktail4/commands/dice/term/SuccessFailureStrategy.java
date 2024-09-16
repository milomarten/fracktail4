package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.Utils;
import lombok.*;

import java.math.BigDecimal;
import java.util.StringJoiner;

/**
 * An alternate totaling strategy where "successes" are counted.
 * Rather than using the face value of each dice directly, the face values are compared
 * to a success threshold. The number of dice that meet or exceed that threshold are returned.
 * as the evaluation's results.
 * A failure threshold can also be specified. If the face value is less than or equal to the
 * failure threshold, these act as subtracting one success each.
 */
@NoArgsConstructor
@Getter
@Setter
public class SuccessFailureStrategy implements DiceTotalingStrategy {
    private int successThreshold = Integer.MAX_VALUE;
    private int failureThreshold = 0;

    @Override
    public TermEvaluationResult compile(DiceExpression.Results results) {
        var expr = new StringJoiner(" + ", "\uD83C\uDFB2(", ")");
        var total = results.getAllResults()
                .<Integer>mapMulti((result, consumer) -> {
                    var value = result.getValue();
                    if (result.isDiscounted()) {
                        expr.add("~~" + value + "~~");
                    } else if (value >= successThreshold) {
                        expr.add("1 [" + value + "]");
                        consumer.accept(1);
                    } else if (value <= failureThreshold) {
                        expr.add("-1 [" + value + "]");
                        consumer.accept(-1);
                    } else {
                        expr.add("0 [" + value + "]");
                        consumer.accept(0);
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new TermEvaluationResult(BigDecimal.valueOf(total), expr.toString());
    }
}
