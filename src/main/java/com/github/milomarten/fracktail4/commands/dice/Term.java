package com.github.milomarten.fracktail4.commands.dice;

public interface Term {
    /**
     * Evaluate this expression, performing all rolls, and returning a result
     * @return A result of all the rolls.
     */
    DiceExpressionEvaluation evaluate() throws DiceExpressionSyntaxError;
}
