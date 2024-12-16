package com.github.milomarten.fracktail4.commands.dice.die;

public class DotStrategy extends SuccessFailureStrategy<Integer> {
    public DotStrategy() {
        super(7, 1);
    }

    @Override
    protected int getCountFor(DiceExpression.Result<Integer> result) {
        if (result.isExploded()) {
            return 2;
        } else {
            return super.getCountFor(result);
        }
    }
}
