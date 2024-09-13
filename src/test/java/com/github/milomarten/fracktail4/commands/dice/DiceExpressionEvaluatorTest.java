package com.github.milomarten.fracktail4.commands.dice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiceExpressionEvaluatorTest {
    @Test
    public void testSimpleAddition() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(1));
        evaluator.pushOperator(Operation.ADD);
        evaluator.pushTerm(HardCodedTerm.of(1));

        var result = evaluator.finish();
        assertEquals(2, result.valueAsInt());
    }

    @Test
    public void testSimpleSubtraction() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(5));
        evaluator.pushOperator(Operation.SUBTRACT);
        evaluator.pushTerm(HardCodedTerm.of(1));

        var result = evaluator.finish();
        assertEquals(4, result.valueAsInt());
    }

    @Test
    public void testMixedPrecedenceOperators() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(5));
        evaluator.pushOperator(Operation.ADD);
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.MULTIPLY);
        evaluator.pushTerm(HardCodedTerm.of(3));

        var result = evaluator.finish();
        assertEquals(11, result.valueAsInt());
    }

    @Test
    public void testMixedPrecedenceWithParenthesis() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushOperator(Operation.LEFT_PARENTHESIS);
        evaluator.pushTerm(HardCodedTerm.of(5));
        evaluator.pushOperator(Operation.ADD);
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.RIGHT_PARENTHESIS);
        evaluator.pushOperator(Operation.MULTIPLY);
        evaluator.pushTerm(HardCodedTerm.of(3));

        var result = evaluator.finish();
        assertEquals(21, result.valueAsInt());
    }

    @Test
    public void testImplicitOneDice() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(20));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result > 0 && result <= 20);
    }

    @Test
    public void testImplicitOneDiceWithOtherOperation() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(3));
        evaluator.pushOperator(Operation.ADD);
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(20));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result > 3 && result <= 23);
    }

    @Test
    public void testMultipleDice() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(10));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 2 && result <= 20);
    }

    @Test
    public void testMultipleDiceFromParenthesisExpression() {
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushOperator(Operation.LEFT_PARENTHESIS);
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.ADD);
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.RIGHT_PARENTHESIS);
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(10));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 4 && result <= 40);
    }

    @Test
    public void testStackedDice() {
        // 2d6 d6
        // [2, 12] d6
        // [2, 72]
        var evaluator = new DiceExpressionEvaluator();
        evaluator.pushTerm(HardCodedTerm.of(2));
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(6));
        evaluator.pushOperator(Operation.DICE);
        evaluator.pushTerm(HardCodedTerm.of(6));

        var result = evaluator.finish().valueAsInt();
        assertTrue(result >= 2 && result <= 72);
    }
}