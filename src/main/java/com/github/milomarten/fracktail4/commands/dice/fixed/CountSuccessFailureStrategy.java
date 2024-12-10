package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.AbstractDiceExpression;
import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceTotalingStrategy;
import com.github.milomarten.fracktail4.commands.dice.Utils;
import com.github.milomarten.fracktail4.commands.dice.die.DiceExpression;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

@Data
public class CountSuccessFailureStrategy<E> implements DiceTotalingStrategy<E> {
    private final String emoji;
    private E successThreshold;
    private E failureThreshold;

    @Override
    public TermEvaluationResult compile(List<AbstractDiceExpression.Result<E>> results, DiceEvaluatorOptions options) {
        var expr = new StringJoiner(" + ", emoji + "(", ")");
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

    protected int getCountFor(DiceExpression.Result<E> result) {
        var value = result.getRoll().getValue();
        if (value.equals(successThreshold)) {
            return 1;
        } else if (value.equals(failureThreshold)) {
            return -1;
        } else {
            return 0;
        }
    }
}
