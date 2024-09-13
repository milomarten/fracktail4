package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.DiceExpressionSyntaxError;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;

public record DiceExpressionEvaluation(BigDecimal value, String representation) {
    /**
     * Get the value as an int.
     * Rounding is okay; everything after the decimal point is discarded. However,
     * if the resulting number is larger than an int, an exception is thrown instead.
     * @return The value, as an integer.
     * @throws DiceExpressionSyntaxError The number is too big.
     */
    public int valueAsInt() throws DiceExpressionSyntaxError {
        try {
            return value.toBigInteger().intValueExact();
        } catch (ArithmeticException ex) {
            throw new DiceExpressionSyntaxError(ex.getMessage());
        }
    }

    public DiceExpressionEvaluation map(UnaryOperator<BigDecimal> mapValue, UnaryOperator<String> mapRep) {
        return new DiceExpressionEvaluation(mapValue.apply(this.value), mapRep.apply(this.representation));
    }
}
