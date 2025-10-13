package com.github.milomarten.fracktail.core.dice.fixed;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.MockFixedDie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FateFixedDiceExpressionTest {
    private final DiceEvaluatorOptions OPTS = DiceEvaluatorOptions.builder().build();

    @Test
    public void testFateDie() {
        var mockDie = new MockFixedDie<>(FateDie.PLUS, FateDie.MINUS);
        var expr = FixedDiceExpression.<FateDie>builder()
                .numberOfDice(2)
                .die(mockDie)
                .build();
        var result = expr.evaluate(OPTS);

        assertNull(result.value());
        assertEquals("+ -", result.representation());
    }
}