package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;

/**
 * Describes a way to turn multiple dice rolls into one final result
 */
public interface DiceTotalingStrategy {
    /**
     * Compile the list of dice rolls into a final result
     * @param results The results to compile
     * @return The final results
     */
    TermEvaluationResult compile(DiceExpression.Results results, DiceEvaluatorOptions options);

    /**
     * Validate this strategy.
     * By default, this does nothing.
     */
    default void validate() {}
}
