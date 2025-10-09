package com.github.milomarten.fracktail.core.dice.term;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;

import java.math.BigDecimal;

public record UnaryOperatorTerm (Term left, Operation symbol, BigDecimal result) implements Term {
    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var leftEval = left.evaluate(options);

        var leftString = leftEval.representation();
        if (!(left instanceof SingleValuedTerm)) {
            leftString = "(" + leftString + ")";
        }

        return new TermEvaluationResult(result, leftString + symbol.getSymbol());
    }

    @Override
    public void validate() {
        left.validate();
    }
}
