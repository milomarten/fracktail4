package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;

@FunctionalInterface
public interface TermOperator {
    Term compute(Term first, Term other, DiceEvaluatorOptions opts);
}
