package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.die.DiceExpression;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DiceExpressionTest {
    @Mock
    private UniformRandomProvider random;

    private static final DiceEvaluatorOptions OPTS = DiceEvaluatorOptions.builder().build();

    private MockDie die(int sides, int... rolls) {
        return new MockDie(sides, rolls);
    }

    @Test
    public void testNormalD20() {
        var dice = DiceExpression.builder()
                .die(die(20, 20))
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(20), response.value());
    }

    @Test
    public void testMultipleD20() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testDrop() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToDrop(1)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(30), response.value());
    }

    @Test
    public void testDropExcessive() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToDrop(100)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.ZERO, response.value());
    }

    @Test
    public void testKeepHighest() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToKeep(2)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(30), response.value());
    }

    @Test
    public void testKeepHighestExcessive() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToKeep(100)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testKeepLowest() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToKeep(2)
                .keepLowest(true)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(21), response.value());
    }

    @Test
    public void testKeepLowestExcessive() {
        var dice = DiceExpression.builder()
                .die(die(20, 18, 12, 9))
                .numberOfDice(3)
                .numberToKeep(100)
                .keepLowest(true)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(39), response.value());
    }

    @Test
    public void testExplodeOnce() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 10, 9))
                .numberOfDice(3)
                .explodeAt(10)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(36), response.value());
    }

    @Test
    public void testExplodeMultiple() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 10, 10, 10, 9))
                .numberOfDice(3)
                .explodeAt(10)
                .infiniteExplode(true)
                .build();
        var response = dice.evaluate(OPTS);

        assertEquals(BigDecimal.valueOf(56), response.value());
    }

    @Test
    public void testExplodeMultipleCap() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 10))
                .numberOfDice(3)
                .explodeAt(10)
                .infiniteExplode(true)
                .build();

        var response = dice.evaluate(OPTS);
        assertEquals(BigDecimal.valueOf(1027), response.value()); // 8 + 9 + 10, plus 100 rerolls of a 10.
    }

    @Test
    public void testRerollOnce() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 1, 8))
                .numberOfDice(3)
                .rerollAt(1)
                .build();

        var response = dice.evaluate(OPTS);
        assertEquals(BigDecimal.valueOf(25), response.value());
    }

    @Test
    public void testRerollMultiple() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 1, 1, 1, 8))
                .numberOfDice(3)
                .rerollAt(1)
                .infiniteReroll(true)
                .build();

        var response = dice.evaluate(OPTS);
        assertEquals(BigDecimal.valueOf(25), response.value());
    }

    @Test
    public void testRerollMultipleCap() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 1))
                .numberOfDice(3)
                .rerollAt(1)
                .infiniteReroll(true)
                .build();

        var response = dice.evaluate(OPTS);
        assertEquals(BigDecimal.valueOf(18), response.value()); // 8 + 9 + (reroll 100 1's) 1
    }

    @Test
    public void testNegativeNumberOfDice() {
        var dice = DiceExpression.builder()
                .die(die(10, 8, 9, 1))
                .numberOfDice(-3)
                .build();

        var response = dice.evaluate(OPTS);
        assertEquals(-1, response.value().signum());
    }
}