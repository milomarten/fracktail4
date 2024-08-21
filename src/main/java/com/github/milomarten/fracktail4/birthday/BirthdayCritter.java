package com.github.milomarten.fracktail4.birthday;

import discord4j.common.util.Snowflake;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class BirthdayCritter {
    private Snowflake critter;
    private MonthDay day;
    private Year year;

    public Optional<Year> getYear() {
        return Optional.ofNullable(year);
    }
}
