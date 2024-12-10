package com.github.milomarten.fracktail4.commands.dice.die;

import com.github.milomarten.fracktail4.commands.dice.term.Status;

public class DotStrategy extends SuccessFailureStrategy<Integer> {
    public DotStrategy() {
        super(7, 1);
    }

    @Override
    protected int getCountFor(DiceExpression.Result<Integer> result) {
        if (result.getRoll().getStatus() == Status.CRITICAL_SUCCESS) {
            return 2;
        } else {
            return super.getCountFor(result);
        }
    }
}
