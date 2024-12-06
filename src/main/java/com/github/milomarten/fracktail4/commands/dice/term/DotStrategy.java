package com.github.milomarten.fracktail4.commands.dice.term;

public class DotStrategy extends SuccessFailureStrategy {
    public DotStrategy() {
        this.setSuccessThreshold(7);
        this.setFailureThreshold(1);
    }

    @Override
    protected int getCountFor(DiceExpression.Result result) {
        if (result.getStatus() == Status.CRITICAL_SUCCESS) {
            return 2;
        } else {
            return super.getCountFor(result);
        }
    }
}
