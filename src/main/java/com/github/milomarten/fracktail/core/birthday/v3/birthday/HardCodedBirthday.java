package com.github.milomarten.fracktail.core.birthday.v3.birthday;

import com.github.milomarten.fracktail.core.birthday.v3.EventAndWhen;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.Set;

public record HardCodedBirthday(String name, MonthDay when, Year yob, Set<Snowflake> guilds) implements BirthdayCalendarEventGenerator, CritterBirthday {
    @Override
    public EventAndWhen<CritterBirthday> createForYear(Year year) {
        return new EventAndWhen<>(this, when.atYear(year.getValue()));
    }

    @Override
    public Mono<Boolean> isValidForServer(GatewayDiscordClient client, Snowflake serverId) {
        return Mono.just(guilds.contains(serverId));
    }

    @Override
    public String displayName() {
        return name;
    }

    @Override
    public Optional<Year> displayYearOfBirth() {
        return Optional.ofNullable(yob);
    }
}
