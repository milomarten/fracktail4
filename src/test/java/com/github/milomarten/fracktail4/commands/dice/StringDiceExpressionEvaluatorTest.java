package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StringDiceExpressionEvaluatorTest {
    private final StringDiceExpressionEvaluator eval = new StringDiceExpressionEvaluator();
    @Test
    public void testNormalSmallInteger() {
        var value = eval.evaluate("5");

        assertEquals(5, value.valueAsInt());
    }

    @Test
    public void testNormalBigInteger() {
        var value = eval.evaluate("187");

        assertEquals(187, value.valueAsInt());
    }

    @Test
    public void testNormalDecimal() {
        var value = eval.evaluate("12.97");

        assertEquals(new BigDecimal("12.97"), value.value());
    }

    @Test
    public void testNormalNumberPositive() {
        var value = eval.evaluate("+12");

        assertEquals(12, value.valueAsInt());
    }

    @Test
    public void testNormalNumberNegative() {
        var value = eval.evaluate("-12");

        assertEquals(-12, value.valueAsInt());
    }

    @Test
    public void testNormalMathAddition() {
        var value = eval.evaluate("3 + 9");

        assertEquals(12, value.valueAsInt());
    }

    @Test
    public void testNormalMathWithNegativeNumbers() {
        var value = eval.evaluate("9 + -3");

        assertEquals(6, value.valueAsInt());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "3 3 +",
            "+ + +",
            "one",
            "+ + 3",
            "d * 2"
    })
    public void testMixedUpExpression(String expression) {
        assertThrows(ExpressionSyntaxError.class, () -> eval.evaluate(expression));
    }

    @ParameterizedTest
    @ValueSource(strings = {
//            "d8 + 3",
            "2d4 * 1d2",
//            "(9 / 3)^d2"
    })
    public void testComplexExpressions(String expression) {
        assertDoesNotThrow(() -> System.out.println(eval.evaluate(expression)));
    }

    @Test
    @DisplayName("(2*d20)/(2d20)+7d4+3")
    public void graphaTestOne() {
        assertDoesNotThrow(() -> {
            var e = eval.evaluate("(2*d20)/(2d20)+7d4+3");

            assertTrue(e.valueAsInt() >= 3);
        });
    }

    @Test
    @DisplayName("d0.5")
    public void graphaTestTwo() {
        assertDoesNotThrow(() -> {
            var e = eval.evaluate("d0.5");
        });
    }
}