package com.github.milomarten.fracktail.core.discord;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

@RequiredArgsConstructor
@Getter
public enum DiscordTimestampFormat {
    RELATIVE("R"),
    SHORT_TIME("t"),
    LONG_TIME("T"),
    SHORT_DATE("d"),
    LONG_DATE("D"),
    LONG_DATE_SHORT_TIME("f"),
    LONG_DATE_DAY_OF_WEEK_SHORT_TIME("F");

    private final String flag;

    public String toDiscord(TemporalAccessor ta) {
        return "<t:" + ta.getLong(ChronoField.INSTANT_SECONDS) + ":" + this.flag + ">";
    }
}
