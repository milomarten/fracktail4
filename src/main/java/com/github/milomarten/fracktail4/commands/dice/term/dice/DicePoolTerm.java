package com.github.milomarten.fracktail4.commands.dice.term.dice;

import com.github.milomarten.fracktail4.commands.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.commands.dice.term.Term;
import com.github.milomarten.fracktail4.commands.dice.term.TermEvaluationResult;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DicePoolTerm implements Term {
    private final List<Term> innerTerms;

    private int keep = Integer.MAX_VALUE;
    private boolean keepLow = false;

    public DicePoolTerm(Term... initial) {
        this.innerTerms = new ArrayList<>(List.of(initial));
    }

    public DicePoolTerm add(Term next) {
        this.innerTerms.add(next);
        return this;
    }

    @Override
    public TermEvaluationResult evaluate(DiceEvaluatorOptions options) throws ExpressionSyntaxError {
        var innerRolls = this.innerTerms.stream()
                .map(t -> t.evaluate(options))
                .map(InnerTermResult::new)
                .toList();

        if (innerRolls.size() > keep) {
            int numberToDrop = innerRolls.size() - keep;
            var comparator = keepLow ?
                    Comparator.<InnerTermResult, BigDecimal>comparing(itr -> itr.result.value().negate()) :
                    Comparator.<InnerTermResult, BigDecimal>comparing(itr -> itr.result.value());
            innerRolls.stream()
                    .sorted(comparator)
                    .limit(numberToDrop)
                    .forEach(itr -> {
                        itr.dropped = true;
                    });
        }

        var representation = innerRolls.stream()
                .map(itr -> {
                    var r = itr.result.representation() + " = " + itr.result.value();
                    if (itr.dropped) {
                        var removeInnerCrossouts = r.replaceAll("~", "");
                        return "~~" + removeInnerCrossouts + "~~";
                    } else {
                        return r;
                    }
                })
                .collect(Collectors.joining(", ", "{", "}"));

        return new TermEvaluationResult(getSingleTermIfPossible(innerRolls).orElse(null),
                representation);
    }

    private Optional<BigDecimal> getSingleTermIfPossible(List<InnerTermResult> results) {
        var nonDiscounted = results
                .stream().filter(r -> !r.dropped).toList();
        if (nonDiscounted.size() == 1) {
            return Optional.of(nonDiscounted.get(0).result.value());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Term keep(Term qty, DiceEvaluatorOptions options) {
        this.keep = qty.evaluate(options).valueAsInt(options.getRoundingMode());
        this.keepLow = false;
        return this;
    }

    @Override
    public Term keepLow(Term qty, DiceEvaluatorOptions options) {
        this.keep = qty.evaluate(options).valueAsInt(options.getRoundingMode());
        this.keepLow = true;
        return this;
    }

    @Override
    public Term comma(Term addl, DiceEvaluatorOptions options) {
        this.innerTerms.add(addl);
        return this;
    }

    @RequiredArgsConstructor
    @Setter
    private static class InnerTermResult {
        private final TermEvaluationResult result;
        private boolean dropped;
    }
}
