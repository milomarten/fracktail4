package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;

import java.math.BigDecimal;

public record BinaryOperatorTerm(Term left, Term right, Operation operation, BigDecimal result) implements Term {
    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var leftEval = left.evaluate(options);
        var rightEval = right.evaluate(options);

        var leftString = leftEval.representation();
        if (!(left instanceof SingleValuedTerm)) {
            leftString = "(" + leftString + ")";
        }
        var rightString = rightEval.representation();
        if (!(right instanceof SingleValuedTerm)) {
            rightString = "(" + rightString + ")";
        }

        return new TermEvaluationResult(result, leftString + " " + operation.getSymbol() + " " + rightString);
    }

    @Override
    public void validate() {
        left.validate();
        right.validate();
    }
}
