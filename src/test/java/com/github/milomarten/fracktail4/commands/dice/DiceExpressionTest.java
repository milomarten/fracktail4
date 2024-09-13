package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.DiceExpression;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiceExpressionTest {
    @Mock
    private RandomGenerator random;

    @Test
    public void testNormalD20() {
        mockRolls(20);
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(20), response.value());
    }

    @Test
    public void testMultipleD20() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testDrop() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToDrop(1)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(30), response.value());
    }

    @Test
    public void testDropExcessive() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToDrop(100)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.ZERO, response.value());
    }

    @Test
    public void testKeepHighest() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToKeep(2)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(30), response.value());
    }

    @Test
    public void testKeepHighestExcessive() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToKeep(100)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testKeepLowest() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToKeep(2)
                .keepLowest(true)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(21), response.value());
    }

    @Test
    public void testKeepLowestExcessive() {
        var dice = DiceExpression.builder()
                .numberOfSides(20)
                .numberOfDice(mockRolls(18, 12, 9))
                .numberToKeep(100)
                .keepLowest(true)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testExplodeOnce() {
        mockRolls(8, 9, 10, 9);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .explodeAt(10)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(36), response.value());
    }

    @Test
    public void testExplodeMultiple() {
        mockRolls(8, 9, 10, 10, 10, 9);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .explodeAt(10)
                .infiniteExplode(true)
                .randomSource(random)
                .build();
        var response = dice.evaluate();

        assertEquals(BigDecimal.valueOf(56), response.value());
    }

    @Test
    public void testExplodeMultipleCap() {
        mockRolls(8, 9, 10);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .explodeAt(10)
                .infiniteExplode(true)
                .randomSource(random)
                .build();

        var response = dice.evaluate();
        assertEquals(BigDecimal.valueOf(1027), response.value()); // 8 + 9 + 10, plus 100 rerolls of a 10.
    }

    @Test
    public void testRerollOnce() {
        mockRolls(8, 9, 1, 8);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .rerollAt(1)
                .randomSource(random)
                .build();

        var response = dice.evaluate();
        assertEquals(BigDecimal.valueOf(25), response.value());
    }

    @Test
    public void testRerollMultiple() {
        mockRolls(8, 9, 1, 1, 1, 8);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .rerollAt(1)
                .infiniteReroll(true)
                .randomSource(random)
                .build();

        var response = dice.evaluate();
        assertEquals(BigDecimal.valueOf(25), response.value());
    }

    @Test
    public void testRerollMultipleCap() {
        mockRolls(8, 9, 1);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(3)
                .rerollAt(1)
                .infiniteReroll(true)
                .randomSource(random)
                .build();

        var response = dice.evaluate();
        assertEquals(BigDecimal.valueOf(18), response.value()); // 8 + 9 + (reroll 100 1's) 1
    }

    @Test
    public void testNegativeNumberOfDice() {
        mockRolls(8, 9, 1);
        var dice = DiceExpression.builder()
                .numberOfSides(10)
                .numberOfDice(-3)
                .randomSource(random)
                .build();

        var response = dice.evaluate();
        assertEquals(-1, response.value().signum());
    }

    private int mockRolls(int value, int... more) {
        when(random.nextInt(anyInt())).thenReturn(value - 1, IntStream.of(more).mapToObj(i -> i - 1).toArray(Integer[]::new));
        return more.length + 1;
    }
}