package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.fixed.Coin;
import com.github.milomarten.fracktail4.commands.dice.fixed.FateDie;
import com.github.milomarten.fracktail4.commands.dice.fixed.FixedDiceExpression;
import com.github.milomarten.fracktail4.commands.dice.fixed.FixedValueDie;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@RequiredArgsConstructor
public enum LetterTerm implements Term {
    COIN('c') {
        @Override
        public Term makeTerm(int numDice) {
            return FixedDiceExpression.<Coin>builder()
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
    };

    private final char letter;

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        return new TermEvaluationResult(null, String.valueOf(letter));
    }

    public abstract Term makeTerm(int numDice);

    public static Optional<LetterTerm> findLetterTerm(char letter) {
        return Arrays.stream(LetterTerm.values())
                .filter(lt -> lt.letter == letter)
                .findFirst();
    }
}
