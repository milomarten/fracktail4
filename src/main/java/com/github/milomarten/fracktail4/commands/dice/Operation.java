package com.github.milomarten.fracktail4.commands.dice;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.BinaryOperator;

@RequiredArgsConstructor
@Getter
public enum Operation {
    ADD("+", 10){
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "addend", "addend", BigDecimal::add);
        }
    },
    SUBTRACT("-", 10) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "minuend", "subtrahend", BigDecimal::subtract);
        }
    },
    MULTIPLY("*", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack,
                    "multiplicand", "multiplier",
                    (one, two) -> one.multiply(two, MC));
        }
    },
    DIVIDE("/", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack,
                    "dividend", "divisor",
                    (one, two) -> one.divide(two, MC));
        }
    },
    LEFT_PARENTHESIS("(", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            throw new DiceExpressionSyntaxError("Unexpectedly encountered a left parenthesis");
        }
    },
    RIGHT_PARENTHESIS(")", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            throw new DiceExpressionSyntaxError("Unexpectedly encountered a right parenthesis");
        }
    },
    // This one is an odd once, since the first parameter is optional.
    // It's the responsibility of the evaluator to add an implicit 1 (for number of dice)
    // to the stack when this operator is preceded specifically by any other operator.
    DICE("d", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            var numberOfSides = Operation.pull(termStack, "number of sides").evaluate();
            var numberOfDice = Operation.pull(termStack, "number of dice").evaluate();
            return DiceExpression.builder()
                    .numberOfSides(numberOfSides.valueAsInt())
                    .numberOfDice(numberOfDice.valueAsInt())
                    .build();
        }
    }
    ;
    private static final MathContext MC = new MathContext(4, RoundingMode.HALF_EVEN);

    private final String symbol;
    private final int priority;

    public abstract Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError;

    private static Term pull(Deque<Term> stack, String descriptor) throws DiceExpressionSyntaxError {
        if (stack.isEmpty()) {
            throw new DiceExpressionSyntaxError("Error pulling " + descriptor + ", no matching term.");
        }
        return stack.pop();
    }

    Term evaluateTwoParameterFunc(Deque<Term> stack, String firstTerm, String secondTerm, BinaryOperator<BigDecimal> operator) {
        var two = Operation.pull(stack, secondTerm).evaluate();
        var one = Operation.pull(stack, firstTerm).evaluate();

        try {
            return new HardCodedTerm(
                    operator.apply(one.value(), two.value()),
                    one.representation() + " " + this.symbol + " " + two.representation()
            );
        } catch (ArithmeticException ex) {
            throw new DiceExpressionSyntaxError(ex.getMessage());
        }
    }

    public static Operation findOperation(String symbol) throws DiceExpressionSyntaxError {
        return Arrays.stream(Operation.values())
                .filter(o -> o.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new DiceExpressionSyntaxError("Symbol " + symbol + " not known"));
    }
}
