package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class Utils {
    private static final int MAX_SCALE = 4;

    public static void checkRange(int value, int low, int high, String field) {
        if (value < low || value > high) {
            throw new ExpressionSyntaxError("Field " + field + " must be in range " + low + " - " + high);
        }
    }

    public static void checkPositive(int value, String field) {
        if (value < 0) {
            throw new ExpressionSyntaxError("Field " + field + " must be positive");
        }
    }

    public static String outputBigDecimal(BigDecimal bd) {
        if (bd.scale() > 4) {
            return bd.setScale(4, RoundingMode.HALF_EVEN)
                    .stripTrailingZeros()
                    .toPlainString();
        }
        return bd.toPlainString();
    }
}
