package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.birthday.v2.UserBirthdayEventInstance;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
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

    public UserBirthdayEventInstance toEvent(GatewayDiscordClient client) {
        return new UserBirthdayEventInstance(this.day, this.year, this.critter, client);
    }
}
