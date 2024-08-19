package com.github.milomarten.fracktail4.birthday;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

@Data
public class BirthdayCritter {
    private final Snowflake critter;
    private final MonthDay birthday;
    private final Year year;

    public Optional<Year> getYear() {
        return Optional.ofNullable(year);
    }
}
