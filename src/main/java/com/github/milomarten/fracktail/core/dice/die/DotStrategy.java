package com.github.milomarten.fracktail.core.dice.die;

public class DotStrategy extends CountSuccessFailureStrategy<Integer> {
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
