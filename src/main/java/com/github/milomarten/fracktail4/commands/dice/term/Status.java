package com.github.milomarten.fracktail4.commands.dice.term;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    NEUTRAL(37, 47),
    CRITICAL_FAIL(31, 41),
    CRITICAL_SUCCESS(32, 42);

    private final int ansiColor;
    private final int ansiDiscountColor;

    public String format(Object roll) {
        return String.format("\u001b[1;%sm%s\u001b[0m", this.ansiColor, roll);
    }

    public String formatDiscounted(Object roll) {
        return String.format("\u001b[%sm%s\u001b[0m", this.ansiDiscountColor, roll);
    }
}
