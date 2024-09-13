package com.github.milomarten.fracktail4.commands.dice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class OperationTest {

    @Test
    public void testAddConstants() {
        var stack = createStack(HardCodedTerm.of(5), HardCodedTerm.of(2));
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
        var stack = createStack(roll, HardCodedTerm.of(2));
        var result = Operation.ADD.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(7), result.value());
        assertEquals("2 + (5)", result.representation());
    }

    @Test
    public void testSubtractConstants() {
        var stack = createStack(HardCodedTerm.of(5), HardCodedTerm.of(2));
        var result = Operation.SUBTRACT.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(-3), result.value());
        assertEquals("2 - 5", result.representation());
    }

    @Test
    public void testMultiplyConstants() {
        var stack = createStack(HardCodedTerm.of(5), HardCodedTerm.of(2));
        var result = Operation.MULTIPLY.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(10), result.value());
        assertEquals("2 * 5", result.representation());
    }

    @Test
    public void testDivideConstants() {
        var stack = createStack(HardCodedTerm.of(5), HardCodedTerm.of(2));
        var result = Operation.DIVIDE.evaluate(stack).evaluate();

        assertEquals(BigDecimal.valueOf(0.4), result.value());
        assertEquals("2 / 5", result.representation());
    }

    @Test
    public void testDivideByZero() {
        var stack = createStack(HardCodedTerm.of(0), HardCodedTerm.of(2));

        assertThrows(DiceExpressionSyntaxError.class, () -> Operation.DIVIDE.evaluate(stack));
    }

    @Test
    public void testDiceOperator() {
        var stack = createStack(HardCodedTerm.of(20), HardCodedTerm.of(2));
        var roll = (DiceExpression) Operation.DICE.evaluate(stack);
        roll.setRandomSource(mockDiceRolls(8, 12));
        var result = roll.evaluate();

        assertEquals(20, result.valueAsInt());
    }

    private static Deque<Term> createStack(Term... items) {
        return new LinkedList<>(Arrays.asList(items));
    }

    private Random mockDiceRolls(Integer dice1, Integer...others) {
        var random = Mockito.mock(Random.class);
        when(random.nextInt(anyInt())).thenReturn(dice1, others);
        return random;
    }
}