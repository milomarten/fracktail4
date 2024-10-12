package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.Utils;

import java.math.BigDecimal;

/**
 * A basic term that has a constant value and expression
 * @param value The value of the term0
 * @param expression The expression to describe the term
 */
public record AccumulationTerm(BigDecimal value, String expression) implements Term {
    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        return new TermEvaluationResult(value, expression);
    }
}
