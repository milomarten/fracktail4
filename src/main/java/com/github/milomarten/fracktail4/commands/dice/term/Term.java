package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceExpressionEvaluation;

/**
 * A generic term than can equal something
 */
public interface Term {
    /**
     * Evaluate this expression, performing all rolls, and returning a result
     * @return A result of all the rolls.
     */
    DiceExpressionEvaluation evaluate() throws DiceExpressionSyntaxError;
}
