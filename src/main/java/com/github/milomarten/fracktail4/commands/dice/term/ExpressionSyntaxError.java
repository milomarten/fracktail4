package com.github.milomarten.fracktail4.commands.dice.term;

/**
 * Indicates an error parsing a dice expression
 */
public class ExpressionSyntaxError extends RuntimeException {
    public ExpressionSyntaxError(String message) {
        super(message);
    }
}
