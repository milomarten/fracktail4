package com.github.milomarten.fracktail4.commands.dice.term;

/**
 * Indicates an error parsing a dice expression
 */
public class DiceExpressionSyntaxError extends RuntimeException {
    public DiceExpressionSyntaxError(String message) {
        super(message);
    }
}
