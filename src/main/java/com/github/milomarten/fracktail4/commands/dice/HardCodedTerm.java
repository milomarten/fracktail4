package com.github.milomarten.fracktail4.commands.dice;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public record HardCodedTerm(BigDecimal value, String expression) implements Term {
    @Override
    public DiceExpressionEvaluation evaluate() throws DiceExpressionSyntaxError {
        return new DiceExpressionEvaluation(value, expression);
    }

    private static final DecimalFormat DF = new DecimalFormat("#.################");
    public static HardCodedTerm of(double value) {
        var bd = BigDecimal.valueOf(value);
        return new HardCodedTerm(bd, bd.toPlainString());
    }

    public static HardCodedTerm of(int value) {
        return new HardCodedTerm(BigDecimal.valueOf(value), Integer.toString(value));
    }
}
