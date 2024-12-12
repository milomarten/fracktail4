package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceTotalingStrategy;
import com.github.milomarten.fracktail4.commands.dice.Utils;
import com.github.milomarten.fracktail4.commands.dice.fixed.CountSuccessFailureStrategy;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
@Getter
@Setter
public class SuccessFailureStrategy<T extends Comparable<T>> extends CountSuccessFailureStrategy<T> {
    private T failureThreshold;

    public SuccessFailureStrategy() {
        super("\uD83C\uDFB2");
    }

    public SuccessFailureStrategy(T successThreshold, T failureThreshold) {
        this();
        this.successThreshold = successThreshold;
        this.failureThreshold = failureThreshold;
    }

    protected int getCountFor(DiceExpression.Result<T> result) {
        var value = result.getRoll().getValue();
        if (successThreshold != null && value.compareTo(successThreshold) >= 0) {
            return 1;
        } else if (failureThreshold != null && value.compareTo(failureThreshold) <= 0) {
            return -1;
        } else {
            return 0;
        }
    }
}
