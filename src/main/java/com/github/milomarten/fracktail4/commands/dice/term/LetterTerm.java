package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.fixed.*;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum LetterTerm implements Term {
    COIN('c') {
        @Override
        public Term makeTerm(int numDice) {
            return CoinExpression.builder()
                    .numberOfDice(numDice)
                    .die(new FixedValueDie<>(Coin.class))
                    .build();
        }
    },
    FATE('f') {
        @Override
        public Term makeTerm(int numDice) {
            return FixedDiceExpression.<FateDie>builder()
                    .numberOfDice(numDice)
                    .die(new FixedValueDie<>(FateDie.class))
                    .build();
        }
    },
    PLUS('+'), MINUS('-'), NEUTRAL('0'), HEADS('H'), TAILS('T');

    private final char letter;

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        throw new ExpressionSyntaxError("Invalid use of character " + this.letter);
    }

    public Term makeTerm(int numDice) {
        throw new ExpressionSyntaxError("Invalid use of character " + this.letter);
    }

    public static Optional<LetterTerm> findLetterTerm(char letter) {
        return Arrays.stream(LetterTerm.values())
                .filter(lt -> lt.letter == letter)
                .findFirst();
    }
}
