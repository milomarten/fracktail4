package com.github.milomarten.fracktail.core.birthday.v2;

import discord4j.common.util.Snowflake;
import lombok.Data;
import reactor.core.publisher.Mono;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;
import java.util.Set;

@Data
public class HardCodedBirthdayEventInstance
        implements BirthdayEventInstance {
    private MonthDay day;
    private Year year;
    private String name;
    private Set<Snowflake> guilds;

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
