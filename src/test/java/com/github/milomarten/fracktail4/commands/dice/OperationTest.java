package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class OperationTest {
    private static final DiceEvaluatorOptions OPTS = DiceEvaluatorOptions.builder().build();

    @Test
    public void testAddConstants() {
        var stack = createStack(ConstantTerm.of(5), ConstantTerm.of(2));
        var result = Operation.ADD.evaluate(stack, OPTS).evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(7), result.value());
        assertEquals("2 + 5", result.representation());
    }

    @Test
    public void testAddDiceRollToConstant() {
        var roll = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(1)
                .randomSource(mockDiceRolls(5))
                .build();
        var stack = createStack(roll, ConstantTerm.of(2));
        var result = Operation.ADD.evaluate(stack, OPTS).evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(7), result.value());
    }

    @Test
    public void testSubtractConstants() {
        var stack = createStack(ConstantTerm.of(5), ConstantTerm.of(2));
        var result = Operation.SUBTRACT.evaluate(stack, OPTS).evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(-3), result.value());
        assertEquals("2 - 5", result.representation());
    }

    @Test
    public void testMultiplyConstants() {
        var stack = createStack(ConstantTerm.of(5), ConstantTerm.of(2));
        var result = Operation.MULTIPLY.evaluate(stack, OPTS).evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(10), result.value());
        assertEquals("2 * 5", result.representation());
    }

    @Test
    public void testDivideConstants() {
        var stack = createStack(ConstantTerm.of(5), ConstantTerm.of(2));
        var result = Operation.DIVIDE.evaluate(stack, OPTS).evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(0.4), result.value());
        assertEquals("2 / 5", result.representation());
    }

    @Test
    public void testDivideByZero() {
        var stack = createStack(ConstantTerm.of(0), ConstantTerm.of(2));

        assertThrows(ExpressionSyntaxError.class, () -> Operation.DIVIDE.evaluate(stack, OPTS));
    }

    @Test
    public void testDiceOperator() {
        var stack = createStack(ConstantTerm.of(20), ConstantTerm.of(2));
        var roll = (DiceExpression) Operation.DICE.evaluate(stack, OPTS);
        roll.setRandomSource(mockDiceRolls(8, 12));
        var result = roll.evaluate(OPTS);

        assertEquals(20, result.valueAsInt());
    }

    @Test
    public void testCeiling() {
        var stack = createStack(ConstantTerm.of(5.4));
        var ceilinged = Operation.CEIL.evaluate(stack, OPTS);

        assertEquals(6, ceilinged.evaluate(OPTS).valueAsInt());
    }

    private static Deque<Term> createStack(Term... items) {
        return new LinkedList<>(Arrays.asList(items));
    }

    private Random mockDiceRolls(int dice1, int...others) {
        var random = Mockito.mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(dice1 - 1,
                IntStream.of(others).mapToObj(i -> i - 1).toArray(Integer[]::new));
        return random;
    }
}