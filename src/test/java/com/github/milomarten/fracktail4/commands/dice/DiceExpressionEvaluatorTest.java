package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.DiceExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.HardCodedTerm;
import com.github.milomarten.fracktail4.commands.dice.term.Operation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiceExpressionEvaluatorTest {
    @Test
    public void testSimpleAddition() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(1));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(1));

        var result = evaluator.finish();
        assertEquals(2, result.valueAsInt());
    }

    @Test
    public void testSimpleSubtraction() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(5));
        evaluator.push(Operation.SUBTRACT);
        evaluator.push(HardCodedTerm.of(1));

        var result = evaluator.finish();
        assertEquals(4, result.valueAsInt());
    }

    @Test
    public void testMixedPrecedenceOperators() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(5));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.MULTIPLY);
        evaluator.push(HardCodedTerm.of(3));

        var result = evaluator.finish();
        assertEquals(11, result.valueAsInt());
    }

    @Test
    public void testMixedPrecedenceWithParenthesis() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(Operation.LEFT_PARENTHESIS);
        evaluator.push(HardCodedTerm.of(5));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.RIGHT_PARENTHESIS);
        evaluator.push(Operation.MULTIPLY);
        evaluator.push(HardCodedTerm.of(3));

        var result = evaluator.finish();
        assertEquals(21, result.valueAsInt());
    }

    @Test
    public void testImplicitOneDice() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(20));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result > 0 && result <= 20);
    }

    @Test
    public void testImplicitOneDiceWithOtherOperation() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(3));
        evaluator.push(Operation.ADD);
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(20));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result > 3 && result <= 23);
    }

    @Test
    public void testMultipleDice() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(10));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 2 && result <= 20);
    }

    @Test
    public void testMultipleDiceFromParenthesisExpression() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(Operation.LEFT_PARENTHESIS);
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.RIGHT_PARENTHESIS);
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(10));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 4 && result <= 40);
    }

    @Test
    public void testStackedDice() {
        // 2d6 d6
        // [2, 12] d6
        // [2, 72]
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(2));
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(6));
        evaluator.push(Operation.DICE);
        evaluator.push(HardCodedTerm.of(6));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 2 && result <= 72);
    }

    @Test
    public void testMisaligned_TwoTerms() {
        assertThrows(DiceExpressionSyntaxError.class, () -> {
            var evaluator = new DiceExpressionEvaluator();
            evaluator.push(HardCodedTerm.of(2));
            evaluator.push(HardCodedTerm.of(4));
        });
    }

    @Test
    public void testMisaligned_TwoOperators() {
        assertThrows(DiceExpressionSyntaxError.class, () -> {
            var evaluator = new DiceExpressionEvaluator();
            evaluator.push(Operation.ADD);
            evaluator.push(Operation.ADD);
        });
    }

    @Test
    public void testCeiling() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(5.4));
        evaluator.push(Operation.CEIL);
        var result = evaluator.finish();

        assertEquals(6, result.valueAsInt());
    }

    @Test
    public void testCeilingPlusNumber() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(5.4));
        evaluator.push(Operation.CEIL);
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(3));
        var result = evaluator.finish();

        assertEquals(9, result.valueAsInt());
    }

    @Test
    public void testLowCap() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(10));
        evaluator.push(Operation.CAP_LOW);
        evaluator.push(HardCodedTerm.of(15));

        var result = evaluator.finish();

        assertEquals(15, result.valueAsInt());
    }

    @Test
    public void testHighCap() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(10));
        evaluator.push(Operation.CAP_HIGH);
        evaluator.push(HardCodedTerm.of(5));

        var result = evaluator.finish();

        assertEquals(5, result.valueAsInt());
    }

    @Test
    public void testLowCapWithOtherOperators() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(HardCodedTerm.of(3));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(10));
        evaluator.push(Operation.CAP_LOW);
        evaluator.push(HardCodedTerm.of(15));

        var result = evaluator.finish();

        assertEquals(18, result.valueAsInt());
    }

    @Test
    public void testLowCapWithParenthesis() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.push(Operation.LEFT_PARENTHESIS);
        evaluator.push(HardCodedTerm.of(8));
        evaluator.push(Operation.ADD);
        evaluator.push(HardCodedTerm.of(10));
        evaluator.push(Operation.RIGHT_PARENTHESIS);
        evaluator.push(Operation.CAP_LOW);
        evaluator.push(HardCodedTerm.of(15));

        var result = evaluator.finish();

        assertEquals(18, result.valueAsInt());
    }
}