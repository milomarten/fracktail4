package com.github.milomarten.fracktail4.birthday.v2;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.Set;

public record HardCodedBirthdayEventInstance(MonthDay day, Year year, String name, Set<Snowflake> guilds)
        implements BirthdayEventInstance {
    @Override
    public Mono<String> getName() {
        return Mono.just(this.name);
    }

    @Override
    public MonthDay getDayOfCelebration() {
        return this.day;
    }

    @Override
    public Optional<Year> getStartYear() {
        return Optional.ofNullable(this.year);
    }

    @Override
    public Mono<Boolean> shouldDisplayForGuild(Snowflake guildId) {
        return Mono.fromSupplier(() -> this.guilds.contains(guildId));
    }
}
