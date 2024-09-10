package com.github.milomarten.fracktail4.birthday.v2;

import discord4j.common.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

public interface BirthdayEventInstance {
    Mono<String> getName();
    MonthDay getDayOfCelebration();
    Optional<Year> getStartYear();

    Mono<Boolean> shouldDisplayForGuild(Snowflake guildId);

    default Mono<Tuple2<BirthdayEventInstance, String>> resolve() {
        return getName()
                .map(name -> Tuples.of(this, name));
    }
}
