package com.github.milomarten.fracktail.core.dice;

import com.github.milomarten.fracktail.core.dice.term.TermEvaluationResult;

import java.util.List;

/**
 * Describes a way to turn multiple dice rolls into one final result
 */
public interface DiceTotalingStrategy<T> {
    /**
     * Compile the list of dice rolls into a final result
     * @param results The results to compile
     * @return The final results
     */
    TermEvaluationResult compile(List<AbstractDiceExpression.Result<T>> results, DiceEvaluatorOptions options);

    /**
     * Validate this strategy.
     * By default, this does nothing.
     */
    default void validate() {}
}
