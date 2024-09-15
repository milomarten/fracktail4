package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {
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
}
