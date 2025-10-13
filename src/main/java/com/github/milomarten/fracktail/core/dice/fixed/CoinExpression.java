package com.github.milomarten.fracktail.core.dice.fixed;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail.core.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail.core.dice.term.LetterTerm;
import com.github.milomarten.fracktail.core.dice.term.Term;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CoinExpression extends FixedDiceExpression<Coin> {
    @Override
    public Term success(Term at, DiceEvaluatorOptions options) {
        CountSuccessStrategy<Coin> fs;
        if (this.totalingStrategy instanceof CountSuccessStrategy<Coin>) {
            fs = (CountSuccessStrategy<Coin>) this.totalingStrategy;
        } else {
            fs = new CountSuccessStrategy<>("\uD83E\uDE99");
        }

        if (at instanceof LetterTerm lt) {
            if (lt == LetterTerm.HEADS) {
                fs.setSuccessThreshold(Coin.HEADS);
                this.totalingStrategy = fs;
                return this;
            } else if (lt == LetterTerm.TAILS) {
                fs.setSuccessThreshold(Coin.TAILS);
                this.totalingStrategy = fs;
                return this;
            }
        }
        throw new ExpressionSyntaxError("Expected H or T, got " + at);
    }
}
