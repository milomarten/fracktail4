package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.roll.DiceExpression;
import com.github.milomarten.fracktail4.commands.dice.roll.DicePoolTerm;
import com.github.milomarten.fracktail4.commands.dice.roll.Die;
import org.apache.commons.rng.UniformRandomProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DicePoolTermTest {
    @Mock
    private UniformRandomProvider random;

    private static final DiceEvaluatorOptions OPTS = DiceEvaluatorOptions.builder().build();

    @Test
    public void twoNormalTerms() {
        var term = new DicePoolTerm(ConstantTerm.of(5), ConstantTerm.of(10));
        var result = term.evaluate(OPTS);

        assertNull(result.value());
        assertEquals("{5 = 5, 10 = 10}", result.representation());
    }

    @Test
    public void twoDiceRolls() {
        var term = new DicePoolTerm(
                makeDice(1, 20),
                makeDice(1, 10)
        );
        when(random.nextInt(anyInt(), anyInt())).thenReturn(10, 5);
        var result = term.evaluate(OPTS);

        assertEquals("{\uD83C\uDFB2(10) = 10, \uD83C\uDFB2(5) = 5}", result.representation());
    }

    @Test
    public void twoNormalTermsKeepHighest() {
        var term = new DicePoolTerm(ConstantTerm.of(5), ConstantTerm.of(10))
                .keep(ConstantTerm.of(1), OPTS);
        var result = term.evaluate(OPTS);

        assertEquals(10, result.valueAsInt(RoundingMode.DOWN));
        assertEquals("{~~5 = 5~~, 10 = 10}", result.representation());
    }

    @Test
    public void twoNormalTermsKeepLowest() {
        var term = new DicePoolTerm(ConstantTerm.of(5), ConstantTerm.of(10))
                .keepLow(ConstantTerm.of(1), OPTS);
        var result = term.evaluate(OPTS);

        assertEquals(5, result.valueAsInt(RoundingMode.DOWN));
        assertEquals("{5 = 5, ~~10 = 10~~}", result.representation());
    }

    private DiceExpression makeDice(int number, int sides) {
        return DiceExpression.builder()
                .numberOfDice(number)
                .die(new Die(sides, random))
                .build();
    }
}