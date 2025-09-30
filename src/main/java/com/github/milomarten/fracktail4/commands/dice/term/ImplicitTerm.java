package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;

import java.math.BigDecimal;

public record ImplicitTerm(BigDecimal bd) implements SingleValuedTerm {
    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        return new TermEvaluationResult(bd, "");
    }
}
