package com.github.milomarten.fracktail4.commands.dice.term;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.function.UnaryOperator;

public record TermEvaluationResult(BigDecimal value, String representation) {
    /**
     * Get the value as an int.
     * Rounding is okay; everything after the decimal point is discarded. However,
     * if the resulting number is larger than an int, an exception is thrown instead.
     * @return The value, as an integer.
     * @throws ExpressionSyntaxError The number is too big.
     */
    public int valueAsInt(RoundingMode roundingMode) throws ExpressionSyntaxError {
        checkForNullValue();
        try {
            return value.setScale(0, roundingMode)
                    .intValue();
        } catch (ArithmeticException ex) {
            throw new ExpressionSyntaxError(ex.getMessage());
        }
    }

    public BigInteger valueAsBigInteger(RoundingMode roundingMode) throws ExpressionSyntaxError {
        checkForNullValue();
        try {
            return value.setScale(0, roundingMode)
                    .toBigInteger();
        } catch (ArithmeticException ex) {
            throw new ExpressionSyntaxError(ex.getMessage());
        }
    }

    public TermEvaluationResult map(UnaryOperator<BigDecimal> mapValue, UnaryOperator<String> mapRep) {
        return new TermEvaluationResult(mapValue.apply(this.value), mapRep.apply(this.representation));
    }

    private void checkForNullValue() {
        if (value == null) {
            throw new ExpressionSyntaxError("Term " + representation + "is not a number, but being treated as one.");
        }
    }
}
