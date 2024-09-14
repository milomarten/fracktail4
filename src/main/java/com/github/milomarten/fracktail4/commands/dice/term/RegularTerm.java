package com.github.milomarten.fracktail4.commands.dice.term;

import java.math.BigDecimal;

/**
 * A basic term that has a constant value and expression
 * @param value The value of the term0
 * @param expression The expression to describe the term
 */
public record RegularTerm(BigDecimal value, String expression) implements Term {
    @Override
    public TermEvaluationResult evaluate() throws ExpressionSyntaxError {
        return new TermEvaluationResult(value, expression);
    }

    /**
     * Create a term from a double.
     * The expression is the value of the double, eliminating all trailing zeroes.
     * @param value The double to wrap.
     * @return The term holding that double.
     */
    public static RegularTerm of(double value) {
        var bd = BigDecimal.valueOf(value);
        return new RegularTerm(bd, bd.toPlainString());
    }

    /**
     * Create a term from an integer.
     * @param value The int to wrap.
     * @return The term holding the int.
     */
    public static RegularTerm of(int value) {
        return new RegularTerm(BigDecimal.valueOf(value), Integer.toString(value));
    }

    /**
     * Create a term from a BigDecimal.
     * The expression is the value of the BigDecimal, eliminating all trailing zeroes.
     * @param value The BigDecimal to wrap.
     * @return The term holding the BigDecimal.
     */
    public static RegularTerm of(BigDecimal value) {
        return new RegularTerm(value, value.toPlainString());
    }
}
