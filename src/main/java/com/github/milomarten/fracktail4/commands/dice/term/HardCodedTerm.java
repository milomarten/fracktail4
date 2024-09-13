package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceExpressionEvaluation;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * A basic term that has a constant value and expression
 * @param value The value of the term
 * @param expression The expression to describe the term
 */
public record HardCodedTerm(BigDecimal value, String expression) implements Term {
    @Override
    public DiceExpressionEvaluation evaluate() throws DiceExpressionSyntaxError {
        return new DiceExpressionEvaluation(value, expression);
    }

    /**
     * Create a term from a double.
     * The expression is the value of the double, eliminating all trailing zeroes.
     * @param value The double to wrap.
     * @return The term holding that double.
     */
    public static HardCodedTerm of(double value) {
        var bd = BigDecimal.valueOf(value);
        return new HardCodedTerm(bd, bd.toPlainString());
    }

    /**
     * Create a term from an integer.
     * @param value The int to wrap.
     * @return The term holding the int.
     */
    public static HardCodedTerm of(int value) {
        return new HardCodedTerm(BigDecimal.valueOf(value), Integer.toString(value));
    }

    /**
     * Create a term from a BigDecimal.
     * The expression is the value of the BigDecimal, eliminating all trailing zeroes.
     * @param value The BigDecimal to wrap.
     * @return The term holding the BigDecimal.
     */
    public static HardCodedTerm of(BigDecimal value) {
        return new HardCodedTerm(value, value.toPlainString());
    }
}
