package com.github.milomarten.fracktail4.commands.dice.term;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Deque;

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
            return evaluateTwoParameterFunc(termStack, options, "addend", "addend", Term::add);
        }
    },
    /**
     * Subtract two terms.
     */
    SUBTRACT("-", 10) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "minuend", "subtrahend", Term::subtract);
        }
    },
    /**
     * Multiply two terms.
     */
    MULTIPLY("*", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options,
                    "multiplicand", "multiplier", Term::multiply);
        }
    },
    /**
     * Divide two terms.
     */
    DIVIDE("/", 8) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options,
                    "dividend", "divisor", Term::divide);
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
        private static final Term ONE = new AccumulationTerm(BigDecimal.ONE, "");

        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            var numberOfSides = Operation.pull(termStack, "number of sides");
            var numberOfDice = Operation.pull(termStack, "number of dice");

            return numberOfDice.dice(numberOfSides, options);
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
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Drop", Term::drop);
        }
    },
    /**
     * Keep the highest n dice rolls.
     */
    KEEP("k", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Keep", Term::keep);
        }
    },
    /**
     * Keep the lowest n dice rolls.
     */
    KEEP_LOWEST("l", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Keep", Term::keepLow);
        }
    },
    /**
     * Reroll (once) any dice lower than n.
     */
    REROLL("r", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Reroll", (a, b, opt) -> a.reroll(b, false, opt));
        }
    },
    /**
     * Reroll (infinitely) any dice lower than n.
     */
    REROLL_INFINITE("R", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Reroll", (a, b, opt) -> a.reroll(b, true, opt));
        }
    },
    /**
     * Roll (once) additional dice for each roll greater than n.
     */
    EXPLODE("e", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Explode", (a, b, opt) -> a.explode(b, false, opt));

        }
    },
    /**
     * Roll (infinitely) additional dice for each roll greater than n.
     */
    EXPLODE_INFINITE("E", 4) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Explode", (a, b, opt) -> a.reroll(b, true, opt));
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
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Success At", Term::success);
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
            return evaluateTwoParameterFunc(termStack, options, "Dice", "Fail At", Term::success);
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
            return term.ceil(options);
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
            return evaluateTwoParameterFunc(termStack, options, "value", "cap", Term::capLow);
        }
    },
    /**
     * Cap the left term to be no more than the right term.
     */
    CAP_HIGH(">", 6) {
        @Override
        public Term evaluate(Deque<Term> termStack, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
            return evaluateTwoParameterFunc(termStack, options, "value", "cap", Term::capHigh);
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

    protected Term evaluateTwoParameterFunc(Deque<Term> stack, DiceEvaluatorOptions options, String firstTerm, String secondTerm, TermOperator operator) {
        var two = Operation.pull(stack, secondTerm);
        var one = Operation.pull(stack, firstTerm);
        return operator.compute(one, two, options);
    }

    public static Operation findOperation(char symbol) throws ExpressionSyntaxError {
        return Arrays.stream(Operation.values())
                .filter(o -> StringUtils.contains(o.symbol, symbol))
                .findFirst()
                .orElseThrow(() -> new ExpressionSyntaxError("Symbol " + symbol + " not known"));
    }
}
