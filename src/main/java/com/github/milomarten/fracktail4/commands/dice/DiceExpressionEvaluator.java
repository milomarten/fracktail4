package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Operation;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Deque;
import java.util.LinkedList;

/**
 * A step-by-step evaluator to turn tokenized infix notation into an expression result.
 * This uses the normal shunting-yard algorithm to handle the work. Each operation pushed
 * will mutate the terms stack, however, so this is a one-time use evaluator.
 */
@RequiredArgsConstructor
public class DiceExpressionEvaluator {
    private final Deque<Term> terms = new LinkedList<>();
    private final Deque<Operation> operators = new LinkedList<>();
    private final DiceEvaluatorOptions options;

    /**
     * Check if this evaluator is expecting a term or an operator
     * If `true`, a term is expected. Otherwise, an operator is expected.
     * This is useful for deciding if positive/negative should be parsed, vs addition/subtraction.
     */
    @Getter private boolean expectingTerm = true; // Sentinel so simple "d20" works

    /**
     * Add a term to the stack.
     * @param term The term to add.
     * @throws ExpressionSyntaxError Pushed two terms in a row
     */
    public void push(Term term) {
        if (!expectingTerm) {
            throw new ExpressionSyntaxError("Was not expecting term " + term);
        }
        this.terms.push(term);
        expectingTerm = false;
    }

    /**
     * Add an operator to the stack.
     * This may mutate the term stack, depending on the combination
     * of operators present on the stack already, as well as the incoming one.
     * @param operator The operator to apply.
     * @throws ExpressionSyntaxError Pushed two operators in a row, or some operator
     * in the stack threw it.
     */
    public void push(Operation operator) {
        if (expectingTerm && operator != Operation.LEFT_PARENTHESIS) {
            this.terms.push(operator.getImplicitLeftTerm());
        }

        if (operator == Operation.LEFT_PARENTHESIS) {
            operators.push(Operation.LEFT_PARENTHESIS);
        } else if (operator == Operation.RIGHT_PARENTHESIS) {
            Operation underneath;
            while (!operators.isEmpty() &&
                    operators.peek() != Operation.LEFT_PARENTHESIS) {
                underneath = operators.pop();
                var result = underneath.evaluate(terms, this.options);
                terms.push(result);
            }
            if (operators.peek() != Operation.LEFT_PARENTHESIS) {
                throw new ExpressionSyntaxError("Mismatched parenthesis");
            }
            operators.pop();
        } else {
            Operation underneath;
            while (!operators.isEmpty() &&
                    operators.peek() != Operation.LEFT_PARENTHESIS &&
                    operators.peek().getPriority() <= operator.getPriority()) {
                underneath = operators.pop();
                var result = underneath.evaluate(terms, this.options);
                terms.push(result);
            }
            operators.push(operator);
        }

        expectingTerm = operator.expectTermAfter();
    }

    /**
     * Indicate that the evaluation should finish.
     * All operators remaining on the stack and popped one by one and applied.
     * By the end, the term stack MUST be 1 element, or a DiceExpressionSyntaxError will be thrown.
     * @return The final evaluation.
     */
    public TermEvaluationResult finish() {
        while (!operators.isEmpty()) {
            Operation op = operators.pop();
            var result = op.evaluate(terms, this.options);
            terms.push(result);
        }

        if (terms.size() != 1) {
            throw new ExpressionSyntaxError("Mismatched operations");
        }

        return terms.pop().evaluate(this.options);
    }
}
