package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * A generic term than can equal something
 */
public interface Term {
    /**
     * Evaluate this expression, performing all rolls, and returning a result
     * @return A result of all the rolls.
     */
    TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError;

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
        // Validation Step - Don't go too high!
        if (a.getDigitCount() + b.getDigitCount() > 18) {
            throw new ExpressionSyntaxError("Multiplying large values. Numbers shouldn't exceed 18 digits.");
        }

        var mult = a.value().multiply(b.value());

        return new AccumulationTerm(mult, a.representation() + " * " + b.representation());
    }

    default Term divide(Term divisor, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = divisor.evaluate(options);
        // Validation Step - Don't go too low.
        if (BigDecimal.ZERO.equals(b.value())) {
            throw new ExpressionSyntaxError("Division by Zero");
        }
        if (a.precisionScore() - b.precisionScore() > 18) {
            throw new ExpressionSyntaxError("Dividing large values. Numbers shouldn't exceed 18 digits.");
        }

        var ratio = a.value().divide(b.value(), MathContext.DECIMAL128);

        return new AccumulationTerm(ratio, a.representation() + " / " + b.representation());
    }

    default Term ceil(DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var ceil = a.value().setScale(0, RoundingMode.CEILING);

        return new AccumulationTerm(ceil, "^" + a.representation());
    }

    default Term capLow(Term lowerBound, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = lowerBound.evaluate(options);

        if (a.value().compareTo(b.value()) < 0) {
            return new AccumulationTerm(b.value(), a.representation() + " < " + b.representation());
        } else {
            return new AccumulationTerm(a.value(), a.representation() + " < " + b.representation());
        }
    }

    default Term capHigh(Term upperBound, DiceEvaluatorOptions options){
        var a = this.evaluate(options);
        var b = upperBound.evaluate(options);

        if (a.value().compareTo(b.value()) > 0) {
            return new AccumulationTerm(b.value(), a.representation() + " > " + b.representation());
        } else {
            return new AccumulationTerm(a.value(), a.representation() + " > " + b.representation());
        }
    }

    default Term dice(Term faces, DiceEvaluatorOptions options){
        var number = this.evaluate(options);
        var facesE = faces.evaluate(options);

        return DiceExpression.builder()
                .numberOfDice(number.valueAsInt())
                .numberOfSides(facesE.valueAsInt())
                .build();
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
}
