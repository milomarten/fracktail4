package com.github.milomarten.fracktail4.commands.dice.term.dice;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.Utils;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

/**
 * An alternate totaling strategy where "successes" are counted.
 * Rather than using the face value of each dice directly, the face values are compared
 * to a success threshold. The number of dice that meet or exceed that threshold are returned.
 * as the evaluation's results.
 * A failure threshold can also be specified. If the face value is less than or equal to the
 * failure threshold, these act as subtracting one success each.
 */
@AllArgsConstructor
@Getter
@Setter
public class SuccessFailureStrategy<T extends Comparable<T>> implements DiceTotalingStrategy<T> {
    private T successThreshold;
    private T failureThreshold;

    @Override
    public TermEvaluationResult compile(List<DiceExpression.Result<T>> results, DiceEvaluatorOptions options) {
        var expr = new StringJoiner(" + ", "\uD83C\uDFB2(", ")");
        var total = results.stream()
                .<Integer>mapMulti((result, consumer) -> {
                    String rollText = result.toString(options.getOutputType());
                    if (result.isDiscounted()) {
                        expr.add(rollText);
                    } else {
                        int count = getCountFor(result);
                        String resolvedText;
                        if (count > 0) {
                            resolvedText = Utils.outputDiceRoll(count, Status.CRITICAL_SUCCESS, options);
                        } else if (count < 0) {
                            resolvedText = Utils.outputDiceRoll(count, Status.CRITICAL_FAIL, options);
                        } else {
                            resolvedText = "0";
                        }
                        expr.add(resolvedText + " [" + rollText + "]");
                        consumer.accept(count);
                    }
                })
                .mapToInt(i -> i)
                .sum();
        return new TermEvaluationResult(BigDecimal.valueOf(total), expr.toString());
    }

    protected int getCountFor(DiceExpression.Result<T> result) {
        var value = result.getRoll().getValue();
        if (value.compareTo(successThreshold) >= 0) {
            return 1;
        } else if (value.compareTo(failureThreshold) <= 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
