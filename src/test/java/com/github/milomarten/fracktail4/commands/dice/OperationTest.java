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

    @Test
    public void testAddConstants() {
        var stack = createStack(RegularTerm.of(5), RegularTerm.of(2));
        var result = Operation.ADD.evaluate(stack).evaluate();

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
        var stack = createStack(roll, RegularTerm.of(2));
        var result = Operation.ADD.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(7), result.value());
    }

    @Test
    public void testSubtractConstants() {
        var stack = createStack(RegularTerm.of(5), RegularTerm.of(2));
        var result = Operation.SUBTRACT.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(-3), result.value());
        assertEquals("2 - 5", result.representation());
    }

    @Test
    public void testMultiplyConstants() {
        var stack = createStack(RegularTerm.of(5), RegularTerm.of(2));
        var result = Operation.MULTIPLY.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(10), result.value());
        assertEquals("2 * 5", result.representation());
    }

    @Test
    public void testDivideConstants() {
        var stack = createStack(RegularTerm.of(5), RegularTerm.of(2));
        var result = Operation.DIVIDE.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(0.4), result.value());
        assertEquals("2 / 5", result.representation());
    }

    @Test
    public void testDivideByZero() {
        var stack = createStack(RegularTerm.of(0), RegularTerm.of(2));

        assertThrows(ExpressionSyntaxError.class, () -> Operation.DIVIDE.evaluate(stack));
    }

    @Test
    public void testDiceOperator() {
        var stack = createStack(RegularTerm.of(20), RegularTerm.of(2));
        var roll = (DiceExpression) Operation.DICE.evaluate(stack);
        roll.setRandomSource(mockDiceRolls(8, 12));
        var result = roll.evaluate();

        assertEquals(20, result.valueAsInt());
    }

    @Test
    public void testCeiling() {
        var stack = createStack(RegularTerm.of(5.4));
        var ceilinged = Operation.CEIL.evaluate(stack);

        assertEquals(6, ceilinged.evaluate().valueAsInt());
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