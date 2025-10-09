package com.github.milomarten.fracktail.core.dice.fixed;

public enum Coin {
    HEADS,
    TAILS;

    private static final String[] vals = {"H", "T"};

    @Override
    public String toString() {
        return vals[ordinal()];
    }
}
