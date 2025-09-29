package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.DiceExpressionConfiguration;
import com.github.milomarten.fracktail4.commands.dice.die.DiceExpression;
import com.github.milomarten.fracktail4.commands.dice.die.DicePoolTerm;
import com.github.milomarten.fracktail4.commands.dice.die.Die;
import com.github.milomarten.fracktail4.commands.dice.fixed.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.OptionalInt;

/**
 * A generic term than can equal something
 */
public interface Term {
    /**
     * Evaluate this expression, performing all rolls, and returning a result
     * @return A result of all the rolls.
     */
    TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError;

    default OptionalInt getNumberOfDiceIfApplicable() { return OptionalInt.empty(); }

    default void validate() {}

    default Term add(Term addend, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = addend.evaluate(options);
        var sum = a.value().add(b.value());

        return new AccumulationTerm(sum, a.representation() + " + " + b.representation());
    }

    default Term subtract(Term minuend, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = minuend.evaluate(options);
        var diff = a.value().subtract(b.value());

        return new AccumulationTerm(diff, a.representation() + " - " + b.representation());
    }

    default Term multiply(Term multiplier, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = multiplier.evaluate(options);

        var mult = a.value().multiply(b.value());

        return new AccumulationTerm(mult, a.representation() + " * " + b.representation());
    }

    default Term divide(Term divisor, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = divisor.evaluate(options);

        if (BigDecimal.ZERO.equals(b.value())) {
            throw new ExpressionSyntaxError("Division by Zero");
        }

        var ratio = a.value().divide(b.value(), MathContext.DECIMAL128);

        return new AccumulationTerm(ratio, a.representation() + " / " + b.representation());
    }

    default Term root(Term radicand, DiceEvaluatorOptions options){
        // n root of x
        // n can only be 1 or more.
        var n = this.evaluate(options);
        var nAsInt = n.valueAsInt(options.getRoundingMode());
        var x = radicand.evaluate(options);

        var squareRootExpression = "(" + n.representation() + ")√(" + x.representation() + ")";
        if (nAsInt == 1) {
            // special case: the 1th root of any number is itself.
            return new AccumulationTerm(x.value(), squareRootExpression);
        } else if (nAsInt == 2) {
            // special case: BigDecimal supports square roots natively.
            return new AccumulationTerm(x.value().sqrt(MathContext.DECIMAL128),
                    squareRootExpression);
        } else if (nAsInt > 0) {
            var power = 1d / nAsInt;
            var result = Math.pow(x.value().doubleValue(), power);
            if (Double.isFinite(result)) {
                return new AccumulationTerm(BigDecimal.valueOf(result),
                        squareRootExpression);
            } else {
                throw new ExpressionSyntaxError("Root operation did not work as expected. The radicand was probably negative");
            }
        } else {
            throw new ExpressionSyntaxError("Can't evaluate a root if the index is nonpositive");
        }
    }

    default Term ceil(DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var ceil = a.value().setScale(0, RoundingMode.CEILING);

        return new AccumulationTerm(ceil, "^" + a.representation());
    }

    default Term dice(Term faces, DiceEvaluatorOptions options){
        var number = this.evaluate(options);

        if (faces instanceof LetterTerm lt) {
            if (lt == LetterTerm.COIN) {
                return CoinExpression.builder()
                        .numberOfDice(number.valueAsInt(options.getRoundingMode()))
                        .die(FixedValueDie.fromEnum(Coin.class))
                        .build();
            } else if (lt == LetterTerm.FATE) {
                return FixedDiceExpression.<FateDie>builder()
                        .numberOfDice(number.valueAsInt(options.getRoundingMode()))
                        .die(FixedValueDie.fromEnum(FateDie.class))
                        .build();
            } else {
                throw new ExpressionSyntaxError("Unexpected dice type " + lt.getLetter());
            }
        } else {
            var facesE = faces.evaluate(options);

            return DiceExpression.builder()
                    .numberOfDice(number.valueAsInt(options.getRoundingMode()))
                    .die(new Die(facesE.valueAsInt(options.getRoundingMode())))
                    .build();
        }
    }

    default Term drop(Term qty, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("drop can only be used on dice expressions");
    }

    default Term keep(Term qty, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("keep can only be used on dice expressions");
    }

    default Term keepLow(Term qty, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("keep-low can only be used on dice expressions");
    }

    default Term reroll(Term at, boolean infinite, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("reroll can only be used on dice expressions");
    }

    default Term explode(Term at, boolean infinite, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("explode can only be used on dice expressions");
    }

    default Term success(Term at, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("success-at can only be used on dice expressions");
    }

    default Term failure(Term at, DiceEvaluatorOptions options){
        throw new ExpressionSyntaxError("failure-at can only be used on dice expressions");
    }

    default Term comma(Term addl, DiceEvaluatorOptions options) {
        return new DicePoolTerm(this, addl);
    }
}
