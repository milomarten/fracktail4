package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.AbstractDiceExpression;
import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceTotalingStrategy;
import com.github.milomarten.fracktail4.commands.dice.die.DiceExpression;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.StringJoiner;

/**
 * A totaling strategy which returns the number of rolls which were exactly some value.
 * @param <E> The type inside the roll.
 */
@Data
public class CountSuccessStrategy<E> implements DiceTotalingStrategy<E> {
    private final String emoji;
    protected E successThreshold;

    @Override
    public TermEvaluationResult compile(List<AbstractDiceExpression.Result<E>> results, DiceEvaluatorOptions options) {
        var expr = new StringJoiner(", ", emoji + "(", ")");
        var total = results.stream()
                .<Integer>mapMulti((result, consumer) -> {
                    String rollText = result.toString(options.getOutputType());
                    if (result.isDropped()) {
                        expr.add(rollText);
                    } else {
                        int count = getCountFor(result);
                        String resolvedText;
                        if (count > 0) {
                            resolvedText = StringUtils.repeat("✅", count);
                        } else if (count < 0) {
                            resolvedText = StringUtils.repeat("❌", -count);
                        } else {
                            resolvedText = "";
                        }
                        expr.add(resolvedText + " " + rollText);
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
        } else {
            return 0;
        }
    }
}
