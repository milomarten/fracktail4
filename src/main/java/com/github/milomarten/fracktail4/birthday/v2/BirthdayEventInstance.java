package com.github.milomarten.fracktail4.birthday.v2;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

public interface BirthdayEventInstance {
    Mono<String> getName();
    MonthDay getDayOfCelebration();
    Optional<Year> getStartYear();

    Mono<Boolean> shouldDisplayForGuild(Snowflake guildId);
}
