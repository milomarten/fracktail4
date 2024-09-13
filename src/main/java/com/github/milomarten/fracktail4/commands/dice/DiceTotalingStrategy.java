package com.github.milomarten.fracktail4.commands.dice;

public interface DiceTotalingStrategy {
    DiceExpressionEvaluation compile(DiceExpression.Results results);
}
