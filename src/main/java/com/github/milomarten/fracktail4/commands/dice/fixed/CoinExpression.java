package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.LetterTerm;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CoinExpression extends FixedDiceExpression<Coin> {
    @Override
    public Term success(Term at, DiceEvaluatorOptions options) {
        CountSuccessFailureStrategy<Coin> fs;
        if (this.totalingStrategy instanceof CountSuccessFailureStrategy<Coin>) {
            fs = (CountSuccessFailureStrategy<Coin>) this.totalingStrategy;
        } else {
            fs = new CountSuccessFailureStrategy<>("\uD83E\uDE99");
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
