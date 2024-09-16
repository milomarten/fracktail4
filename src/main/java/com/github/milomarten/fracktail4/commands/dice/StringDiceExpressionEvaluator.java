package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Operation;
import com.github.milomarten.fracktail4.commands.dice.term.RegularTerm;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

@Component
public class StringDiceExpressionEvaluator {
    public TermEvaluationResult evaluate(String expression) throws ExpressionSyntaxError {
        return evaluate(expression, DiceEvaluatorOptions.builder().build());
    }

    public TermEvaluationResult evaluate(String expression, DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var evaluator = new DiceExpressionEvaluator(options);
        var iterator = new StringCharacterIterator(expression);

        for(char c = iterator.first(); c != CharacterIterator.DONE; ) {
            if (Character.isWhitespace(c)) {
                c = iterator.next(); // Drop it
            } else if (isNumber(c) || ((c == '+' || c == '-') && evaluator.isExpectingTerm())) {
                var value = tryParseNumberFromIterator(iterator);
                evaluator.push(RegularTerm.of(value));
                c = iterator.current();
            } else {
                var operator = Operation.findOperation(c);
                evaluator.push(operator);
                c = iterator.next();
            }
        }

        return evaluator.finish();
    }

    private boolean isNumber(char c) {
        return  c >= '0' && c <= '9';
    }

    private boolean isNumberRelatedCharacter(char c) {
        return c == '.';
    }

    private BigDecimal tryParseNumberFromIterator(CharacterIterator iter) {
        StringBuilder sb = new StringBuilder();
        sb.append(iter.current());

        while (iter.next() != CharacterIterator.DONE &&
                (isNumber(iter.current()) || isNumberRelatedCharacter(iter.current()))) {
            sb.append(iter.current());
        }
        try {
            return new BigDecimal(sb.toString());
        } catch (NumberFormatException ex) {
            throw new ExpressionSyntaxError("Unknown number " + sb);
        }
    }
}
