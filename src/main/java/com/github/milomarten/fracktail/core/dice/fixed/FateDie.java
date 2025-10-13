package com.github.milomarten.fracktail.core.dice.fixed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents the value of a fate die, which can be +, -, or 0.
 */
@RequiredArgsConstructor
@Getter
public enum FateDie {
    PLUS,
    MINUS,
    NEUTRAL;

    private static final String[] vals = {"+", "-", "0"};

    @Override
    public String toString() {
        return vals[ordinal()];
    }
}
