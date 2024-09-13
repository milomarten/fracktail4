package com.github.milomarten.fracktail4.commands.dice;

import java.util.Deque;
import java.util.LinkedList;

public class DiceExpressionEvaluator {
    private final Deque<Term> terms = new LinkedList<>();
    private final Deque<Operation> operators = new LinkedList<>();

    private boolean operatorLast = true; // Sentinel so simple "d20" works

    public void pushTerm(Term term) {
        this.terms.push(term);
        operatorLast = false;
    }

    public void pushOperator(Operation operator) {
        if (operator == Operation.DICE && operatorLast) {
            // Add the implicit 1.
            this.terms.push(HardCodedTerm.of(1));
        }

        if (operator == Operation.LEFT_PARENTHESIS) {
            operatorLast = true;
            operators.push(Operation.LEFT_PARENTHESIS);
        } else if (operator == Operation.RIGHT_PARENTHESIS) {
            operatorLast = false;
            Operation underneath;
            while (!operators.isEmpty() &&
                    operators.peek() != Operation.LEFT_PARENTHESIS) {
                underneath = operators.pop();
                var result = underneath.evaluate(terms);
                terms.push(result);
            }
            if (operators.peek() != Operation.LEFT_PARENTHESIS) {
                throw new DiceExpressionSyntaxError("Mismatched parenthesis");
            }
            operators.pop();
        } else {
            operatorLast = true;
            Operation underneath;
            while (!operators.isEmpty() &&
                    operators.peek() != Operation.LEFT_PARENTHESIS &&
                    operators.peek().getPriority() <= operator.getPriority()) {
                underneath = operators.pop();
                var result = underneath.evaluate(terms);
                terms.push(result);
            }
            operators.push(operator);
        }
    }

    public DiceExpressionEvaluation finish() {
        while (!operators.isEmpty()) {
            Operation op = operators.pop();
            var result = op.evaluate(terms);
            terms.push(result);
        }

        if (terms.size() != 1) {
            throw new DiceExpressionSyntaxError("Mismatched operations");
        }

        return terms.pop().evaluate();
    }
}
