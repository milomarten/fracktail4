package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

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

    public static String outputBigDecimal(BigDecimal bd) {
        if (bd.scale() > DiceExpressionConfiguration.MAX_DIGITS_FRACTION_PART) {
            return bd.setScale(DiceExpressionConfiguration.MAX_DIGITS_FRACTION_PART, RoundingMode.HALF_EVEN)
                    .stripTrailingZeros()
                    .toPlainString();
        }
        return bd.toPlainString();
    }

    public static void doNTimes(long number, Runnable action) {
        if (number == 0) return;
        LongStream.range(0, number).forEach(i -> action.run());
    }

    public static <T> List<T> doNTimes(int number, Supplier<T> action) {
        if (number == 0) return List.of();
        return IntStream.range(0, number).mapToObj(i -> action.get()).toList();
    }

    public static int numberOfIntegerDigits(BigDecimal bd) {
        return bd.precision() - bd.scale();
    }
}
