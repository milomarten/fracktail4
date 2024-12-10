package com.github.milomarten.fracktail4.commands.dice.fixed;

import com.github.milomarten.fracktail4.commands.dice.term.Status;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FateDie implements FixedValue {
    PLUS(Status.CRITICAL_SUCCESS),
    MINUS(Status.CRITICAL_FAIL),
    NEUTRAL(Status.NEUTRAL);

    private final Status status;

    private static final String[] vals = {"``+``", "``-``", "``0``"};

    @Override
    public String toString() {
        return vals[ordinal()];
    }
}
