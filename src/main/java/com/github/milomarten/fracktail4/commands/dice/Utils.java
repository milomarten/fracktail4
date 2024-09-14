package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {
    public static void checkPositiveAndLessThan(int value, int high, String field) {
        if (value < 0 || value > high) {
            throw new ExpressionSyntaxError("Field " + field + " must be in range 0" + " - " + high);
        }
    }

    public static void checkPositive(int value, String field) {
        if (value < 0) {
            throw new ExpressionSyntaxError("Field " + field + " must be positive");
        }
    }
}
