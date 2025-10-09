package com.github.milomarten.fracktail.core.dice.term;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
public enum LetterTerm implements Term {
    COIN('c'),
    FATE('f'),
    HEADS('H'),
    TAILS('T');

    private final char letter;

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        throw new ExpressionSyntaxError("Invalid use of character " + this.letter);
    }

    public static Optional<LetterTerm> findLetterTerm(char letter) {
        return Arrays.stream(LetterTerm.values())
                .filter(lt -> lt.letter == letter || Character.toUpperCase(lt.letter) == letter)
                .findFirst();
    }
}
