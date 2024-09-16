package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "addend", "addend", BigDecimal::add);
        }
    },
    /**
     * Subtract two terms.
     */
    SUBTRACT("-", 10) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "minuend", "subtrahend", BigDecimal::subtract);
        }
    },
    /**
     * Multiply two terms.
     */
    MULTIPLY("*", 8) {
        private static final MathContext MC = MathContext.DECIMAL128;

        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options,
                    "multiplicand", "multiplier",
                    (one, two) -> {
                        preValidate(one, two);
                        return one.multiply(two, MC);
                    });
        }

        private void preValidate(BigDecimal one, BigDecimal two) {
            var digitCountOne = one.signum() == 0 ? 1 : one.precision() - one.scale();
            var digitCountTwo = one.signum() == 0 ? 1 : two.precision() - two.scale();

            if (digitCountOne + digitCountTwo > 18) {
                throw new ExpressionSyntaxError("Multiplying large values. Numbers shouldn't exceed 18 digits.");
            }
        }
    },
    /**
     * Divide two terms.
     */
    DIVIDE("/", 8) {
        private static final MathContext MC = MathContext.DECIMAL128;

        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options,
                    "dividend", "divisor",
                    (one, two) -> {
                        preValidate(one, two);
                        return one.divide(two, MC);
                    });
        }

        private void preValidate(BigDecimal one, BigDecimal two) {
            var digitCountOne = precisionScore(one);
            var digitCountTwo = precisionScore(two);

            if (digitCountOne - digitCountTwo > 18) {
                throw new ExpressionSyntaxError("Dividing large values. Numbers shouldn't exceed 18 digits.");
            }
        }

        private int precisionScore(BigDecimal bd) {
            if (bd.signum() == 0) {
                return 1;
            } else {
                var digitsToTheLeft = bd.precision() - bd.scale();
                if (digitsToTheLeft == 0) {
                    return -bd.scale();
                } else {
                    return digitsToTheLeft;
                }
            }
        }
    },
    /**
     * Marker for a left parenthesis.
     * If evaluated, always throws an exception.
     */
    LEFT_PARENTHESIS("([", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            throw new ExpressionSyntaxError("Unexpectedly encountered a left parenthesis");
        }
    },
    /**
     * Marker for a right parenthesis.
     * If evaluated, always throws an exception.
     */
    RIGHT_PARENTHESIS(")]", 0) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            throw new ExpressionSyntaxError("Unexpectedly encountered a right parenthesis");
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
        private static final Term ONE = new RegularTerm(BigDecimal.ONE, "");

        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            var numberOfSides = Operation.pull(termStack, "number of sides").evaluate(options);
            var numberOfDice = Operation.pull(termStack, "number of dice").evaluate(options);
            return DiceExpression.builder()
                    .numberOfSides(numberOfSides.valueAsInt())
                    .numberOfDice(numberOfDice.valueAsInt())
                    .build();
        }

        @Override
        public Term getImplicitLeftTerm() throws ExpressionSyntaxError {
            return ONE;
        }
    },
    /**
     * Drop the lowest n dice rolls
     */
    DROP("x", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Drop", DiceExpression::setNumberToDrop);
        }
    },
    /**
     * Keep the highest n dice rolls.
     */
    KEEP("k", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Keep", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Keep Lowest", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Reroll", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Reroll Infinite", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Explode", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Explode Infinite", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Success At", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateDiceIntegerFunc(termStack, options, "Success At", (expr, i) -> {
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            var term = Operation.pull(termStack, "Term to ceil");
            var evaluated = term.evaluate(options);
            var ceiling = evaluated.value().setScale(0, RoundingMode.CEILING);

            return new RegularTerm(ceiling, "^" + evaluated.representation());
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "value", "cap",
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
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "value", "cap",
                    (number, cap) -> {
                        if (number.compareTo(cap) > 0) {
                            return cap;
                        }
                        return number;
                    });
        }
    }
    ;

    private final String symbol;
    private final int priority;

    /**
     * Invoke this operator on the term stack to create a new term.
     * @param termStack The stack to mutate
     * @return A term which is the result of this operator acting on the stack
     * @throws ExpressionSyntaxError The operator encountered a stack it could not handle.
     */
    public abstract Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options);

    /**
     * Get the implicit left term of this operation.
     * This is to support operators that can have either one or two operations, such as DICE.
     * If an operator doesn't implement this, the default is to throw an exception.
     * @return The left term that should be used as a suitable default.
     * @throws ExpressionSyntaxError An implicit left term is unsupported.
     */
    public Term getImplicitLeftTerm() throws ExpressionSyntaxError {
        throw new ExpressionSyntaxError("Was not expecting operation " + this);
    }

    public boolean expectTermAfter() {
        return true;
    }

    private static Term pull(Deque<Term> stack, String descriptor) throws ExpressionSyntaxError {
        if (stack.isEmpty()) {
            throw new ExpressionSyntaxError("Error pulling " + descriptor + ", no matching term.");
        }
        return stack.pop();
    }

    protected Term evaluateTwoParameterFunc(Deque<Term> stack, DiceEvaluatorOptions options, String firstTerm, String secondTerm, BinaryOperator<BigDecimal> operator) {
        var two = Operation.pull(stack, secondTerm).evaluate(options);
        var one = Operation.pull(stack, firstTerm).evaluate(options);

        try {
            return new RegularTerm(
                    operator.apply(one.value(), two.value()),
                    one.representation() + " " + this.symbol + " " + two.representation()
            );
        } catch (ArithmeticException ex) {
            throw new ExpressionSyntaxError(ex.getMessage());
        }
    }

    protected Term evaluateDiceIntegerFunc(Deque<Term> stack, DiceEvaluatorOptions options, String term, BiConsumer<DiceExpression, Integer> func) {
        var two = Operation.pull(stack, term).evaluate(options);
        var one = Operation.pull(stack, "Dice");

        if (one instanceof DiceExpression dice) {
            func.accept(dice, two.valueAsInt());
            return one;
        }
        throw new ExpressionSyntaxError(term + " can only be used on dice expressions");
    }

    public static Operation findOperation(char symbol) throws ExpressionSyntaxError {
        return Arrays.stream(Operation.values())
                .filter(o -> StringUtils.contains(o.symbol, symbol))
                .findFirst()
                .orElseThrow(() -> new ExpressionSyntaxError("Symbol " + symbol + " not known"));
    }
}
