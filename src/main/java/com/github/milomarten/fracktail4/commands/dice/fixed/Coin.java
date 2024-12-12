package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.term.Status;

public enum Coin implements DieValue {
    HEADS,
    TAILS;

    @Override
    public Status getStatus() {
        return Status.NEUTRAL;
    }

    private static final String[] vals = {"H", "T"};

    @Override
    public String toString() {
        return vals[ordinal()];
    }
}
