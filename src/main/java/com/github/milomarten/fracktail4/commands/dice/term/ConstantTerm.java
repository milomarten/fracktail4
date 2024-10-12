package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.Utils;

import java.math.BigDecimal;

public record ConstantTerm(BigDecimal bd) implements Term {
    /**
     * Create a term from a double.
     * The expression is the value of the double, eliminating all trailing zeroes.
     * @param value The double to wrap.
     * @return The term holding that double.
     */
    public static ConstantTerm of(double value) {
        return new ConstantTerm(BigDecimal.valueOf(value));
    }

    /**
     * Create a term from an integer.
     * @param value The int to wrap.
     * @return The term holding the int.
     */
    public static ConstantTerm of(int value) {
        return new ConstantTerm(BigDecimal.valueOf(value));
    }

    /**
     * Create a term from a BigDecimal.
     * The expression is the value of the BigDecimal, eliminating all trailing zeroes.
     * @param value The BigDecimal to wrap.
     * @return The term holding the BigDecimal.
     */
    public static ConstantTerm of(BigDecimal value) {
        return new ConstantTerm(value);
    }

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        return new TermEvaluationResult(this.bd, Utils.outputBigDecimal(this.bd));
    }
}
