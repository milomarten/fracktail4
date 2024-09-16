package com.github.milomarten.fracktail4.commands.dice.term;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    NEUTRAL(0),
    CRITICAL_FAIL(31),
    CRITICAL_SUCCESS(32);

    private final int ansiColor;

    public String format(int roll) {
        if (this.ansiColor == 0) {
            return String.valueOf(roll);
        }
        return String.format("\u001b[1;%dm%s\u001b[0m", this.ansiColor, roll);
    }
}
