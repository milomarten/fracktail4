package com.github.milomarten.fracktail.core.dice.term;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;

import java.math.BigDecimal;

public record BinaryOperatorTerm(TermEvaluationResult left, TermEvaluationResult right, Operation operation, BigDecimal result) implements Term {
    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var leftString = left.representation();
        var rightString = right.representation();

        return new TermEvaluationResult(result, leftString + " " + operation.getSymbol() + " " + rightString);
    }
}
