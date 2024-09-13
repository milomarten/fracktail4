package com.github.milomarten.fracktail4.commands.dice.term;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Deque;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;

/**
 * Describes all the operations supported by the bot.
 * In addition to add, subtract, multiply, divide, and parenthesis, dice-related
 * "operations" are provided.
 * <br>
 * DICE can be used to initialize a dice roll (left term is optional,
 * or is the number of dice to roll; right term is the number of faces), while subsequent dice operations
 * describe ways to enhance the initial dice roll. This approach allows dynamic values for all
 * operations.
 * All dice operations are highest priority, aside from parenthesis.
 * As an example, 4 + 2d8 is treated as "2d8, plus 4", rather than "6d8". All dice operations
 * will also cast their arguments into integers, by chopping any decimal places off. If you would
 * prefer to round up, consider using the "^" operator after the term.
 * Attempting to use a dice operation on a constant will throw an exception.
 * <br>
 * There are also some special operators, to handle specific use cases. These are all lowest priority.
 * ^ (CEIL) - Rounds the value up to the nearest 1.
 */
@RequiredArgsConstructor
@Getter
public enum Operation {
    // Normal Math
    /**
     * Add two terms.
     */
    ADD("+", 10){
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "addend", "addend", BigDecimal::add);
        }
    },
    /**
     * Subtract two terms.
     */
    SUBTRACT("-", 10) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "minuend", "subtrahend", BigDecimal::subtract);
        }
    },
    /**
     * Multiply two terms.
     */
    MULTIPLY("*", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack,
                    "multiplicand", "multiplier",
                    (one, two) -> one.multiply(two, MC));
        }
    },
    /**
     * Divide two terms.
     */
    DIVIDE("/", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack,
                    "dividend", "divisor",
                    (one, two) -> one.divide(two, MC));
        }
    },
    /**
     * Marker for a left parenthesis.
     * If evaluated, always throws an exception.
     */
    LEFT_PARENTHESIS("([", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            throw new DiceExpressionSyntaxError("Unexpectedly encountered a left parenthesis");
        }
    },
    /**
     * Marker for a right parenthesis.
     * If evaluated, always throws an exception.
     */
    RIGHT_PARENTHESIS(")]", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            throw new DiceExpressionSyntaxError("Unexpectedly encountered a right parenthesis");
        }

        @Override
        public boolean expectTermAfter() {
            return false;
        }
    },
    // Dice Operators
    /**
     * Initializes a Dice term.
     * The evaluator specifically handles this case, in order to support the optional "left"
     * parameter. If the parameter is optional, a 1 is implied.
     */
    DICE("d", 4) {
        // This special ONE makes the parsing logic easier, without showing an unexpected 1,
        // when "d" is used with no left term.
        private static final Term ONE = new HardCodedTerm(BigDecimal.ONE, "");

        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            var numberOfSides = Operation.pull(termStack, "number of sides").evaluate();
            var numberOfDice = Operation.pull(termStack, "number of dice").evaluate();
            return DiceExpression.builder()
                    .numberOfSides(numberOfSides.valueAsInt())
                    .numberOfDice(numberOfDice.valueAsInt())
                    .build();
        }

        @Override
        public Term getImplicitLeftTerm() throws DiceExpressionSyntaxError {
            return ONE;
        }
    },
    /**
     * Drop the lowest n dice rolls
     */
    DROP("x", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Drop", DiceExpression::setNumberToDrop);
        }
    },
    /**
     * Keep the highest n dice rolls.
     */
    KEEP("k", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Keep", (expr, i) -> {
                expr.setNumberToKeep(i);
                expr.setKeepLowest(false);
            });
        }
    },
    /**
     * Keep the lowest n dice rolls.
     */
    KEEP_LOWEST("l", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Keep Lowest", (expr, i) -> {
                expr.setNumberToKeep(i);
                expr.setKeepLowest(true);
            });
        }
    },
    /**
     * Reroll (once) any dice lower than n.
     */
    REROLL("r", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Reroll", (expr, i) -> {
                expr.setRerollAt(i);
                expr.setInfiniteReroll(false);
            });
        }
    },
    /**
     * Reroll (infinitely) any dice lower than n.
     */
    REROLL_INFINITE("R", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Reroll Infinite", (expr, i) -> {
                expr.setRerollAt(i);
                expr.setInfiniteReroll(true);
            });
        }
    },
    /**
     * Roll (once) additional dice for each roll greater than n.
     */
    EXPLODE("e", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Explode", (expr, i) -> {
                expr.setExplodeAt(i);
                expr.setInfiniteExplode(false);
            });
        }
    },
    /**
     * Roll (infinitely) additional dice for each roll greater than n.
     */
    EXPLODE_INFINITE("E", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Explode Infinite", (expr, i) -> {
                expr.setExplodeAt(i);
                expr.setInfiniteReroll(true);
            });
        }
    },
    /**
     * If present, this dice roll enters Success Counting mode.
     * Rather than adding the face value of each dice, the result will instead be the number
     * of dice that are greater than or equal to n.
     * As an example, 4d6 rolls 2, 3, 6, 1. Total = 12. If SUCCESS_AT 3, however, Total = 2, since there
     * are two dice greater than or equal to 3.
     */
    SUCCESS_AT("s", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Success At", (expr, i) -> {
                if (expr.getTotalingStrategy() instanceof SuccessFailureStrategy sfs) {
                    sfs.setSuccessThreshold(i);
                } else {
                    var sfs = new SuccessFailureStrategy();
                    sfs.setSuccessThreshold(i);
                    expr.setTotalingStrategy(sfs);
                }
            });
        }
    },
    /**
     * If present, this dice roll enters Success Counting mode.
     * Rather than adding the face value of each dice, the result will instead be the negative of
     * the number of dice that are less than or equal to n.
     * As an example, 4d6 rolls 2, 3, 6, 1. Total = 12. If FAILURE_AT 2, however, Total = -2, since there
     * are two dice less than or equal to 2.
     * This stacks with SUCCESS_AT. For example, if SUCCESS_AT is 3, and FAILURE_AT is 1, then TOTAL is 1:
     * there are two dice greater than 3, but one less than 1.
     */
    FAILURE_AT("f", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, "Success At", (expr, i) -> {
                if (expr.getTotalingStrategy() instanceof SuccessFailureStrategy sfs) {
                    sfs.setFailureThreshold(i);
                } else {
                    var sfs = new SuccessFailureStrategy();
                    sfs.setFailureThreshold(i);
                    expr.setTotalingStrategy(sfs);
                }
            });
        }
    },
    // Special commands
    /**
     * Round the term up to the nearest whole number.
     * This can be used to counter the native behavior of rounding down for dice operations.
     */
    CEIL("^", 6) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            var term = Operation.pull(termStack, "Term to ceil");
            var evaluated = term.evaluate();
            var ceiling = evaluated.value().setScale(0, RoundingMode.CEILING);

            return new HardCodedTerm(ceiling, "^" + evaluated);
        }

        @Override
        public boolean expectTermAfter() {
            return false;
        }
    },
    /**
     * Cap the left term to be no less than the right term.
     */
    CAP_LOW("<", 6) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "value", "cap",
                    (number, cap) -> {
                        if (number.compareTo(cap) < 0) {
                            return cap;
                        }
                        return number;
                    });
        }
    },
    /**
     * Cap the left term to be no more than the right term.
     */
    CAP_HIGH(">", 6) {
        @Override
        public Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, "value", "cap",
                    (number, cap) -> {
                        if (number.compareTo(cap) > 0) {
                            return cap;
                        }
                        return number;
                    });
        }
    }
    ;
    private static final MathContext MC = new MathContext(4, RoundingMode.HALF_EVEN);

    private final String symbol;
    private final int priority;

    /**
     * Invoke this operator on the term stack to create a new term.
     * @param termStack The stack to mutate
     * @return A term which is the result of this operator acting on the stack
     * @throws DiceExpressionSyntaxError The operator encountered a stack it could not handle.
     */
    public abstract Term evaluate(Deque<Term> termStack) throws DiceExpressionSyntaxError;

    /**
     * Get the implicit left term of this operation.
     * This is to support operators that can have either one or two operations, such as DICE.
     * If an operator doesn't implement this, the default is to throw an exception.
     * @return The left term that should be used as a suitable default.
     * @throws DiceExpressionSyntaxError An implicit left term is unsupported.
     */
    public Term getImplicitLeftTerm() throws DiceExpressionSyntaxError {
        throw new DiceExpressionSyntaxError("Was not expecting operation " + this);
    }

    public boolean expectTermAfter() {
        return true;
    }

    private static Term pull(Deque<Term> stack, String descriptor) throws DiceExpressionSyntaxError {
        if (stack.isEmpty()) {
            throw new DiceExpressionSyntaxError("Error pulling " + descriptor + ", no matching term.");
        }
        return stack.pop();
    }

    protected Term evaluateTwoParameterFunc(Deque<Term> stack, String firstTerm, String secondTerm, BinaryOperator<BigDecimal> operator) {
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

    protected Term evaluateDiceIntegerFunc(Deque<Term> stack, String term, BiConsumer<DiceExpression, Integer> func) {
        var two = Operation.pull(stack, term).evaluate();
        var one = Operation.pull(stack, "Dice");

        if (one instanceof DiceExpression dice) {
            func.accept(dice, two.valueAsInt());
            return one;
        }
        throw new DiceExpressionSyntaxError(term + " can only be used on dice expressions");
    }

    public static Operation findOperation(String symbol) throws DiceExpressionSyntaxError {
        return Arrays.stream(Operation.values())
                .filter(o -> o.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new DiceExpressionSyntaxError("Symbol " + symbol + " not known"));
    }
}
