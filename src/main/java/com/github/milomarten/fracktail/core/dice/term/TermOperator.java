package com.github.milomarten.fracktail.core.dice.term;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;

@FunctionalInterface
public interface TermOperator {
    Term compute(Term first, Term other, DiceEvaluatorOptions opts);
}
