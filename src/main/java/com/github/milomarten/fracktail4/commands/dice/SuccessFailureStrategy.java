package com.github.milomarten.fracktail4.commands.dice;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SuccessFailureStrategy implements DiceTotalingStrategy {
    private int successThreshold;
    private int failureThreshold;

    /**
     * Create a strategy with a success threshold and no failure threshold.
     * @param threshold The number that counts as a success
     * @return The strategy.
     */
    public static SuccessFailureStrategy onSuccess(int threshold) {
        return new SuccessFailureStrategy(threshold, Integer.MIN_VALUE);
    }

    /**
     * Create a strategy with a success threshold and failure threshold.
     * @param successThreshold The number that counts as a success
     * @param failureThreshold The number that counts as a failure
     * @return The strategy.
     */
    public static SuccessFailureStrategy onSuccessFailure(int successThreshold, int failureThreshold) {
        return new SuccessFailureStrategy(successThreshold, failureThreshold);
    }

    @Override
    public DiceExpressionEvaluation compile(DiceExpression.Results results) {
        var expr = new StringJoiner(" + ", "(", ")");
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
        return new DiceExpressionEvaluation(BigDecimal.valueOf(total), expr.toString());
    }
}
