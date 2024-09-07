package com.github.milomarten.fracktail4.birthday.v2;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

import java.time.MonthDay;
import java.time.Year;
import java.util.Optional;

public record UserBirthdayEventInstance(MonthDay time, Year year, Snowflake userId, GatewayDiscordClient client)
        implements BirthdayEventInstance {
    @Override
    public Mono<String> getName() {
        return this.client.getUserById(this.userId)
                .map(User::getUsername);
    }

    @Override
    public MonthDay getDayOfCelebration() {
        return this.time;
    }

    @Override
    public Optional<Year> getStartYear() {
        return Optional.ofNullable(this.year);
    }

    @Override
    public Mono<Boolean> shouldDisplayForGuild(Snowflake guildId) {
        // Display if they are a member of this guild.
        // Potential future enhancement: per-guild opt in by the user.
        return client.getMemberById(guildId, userId)
                .map(member -> true)
                .onErrorResume(ex -> Mono.just(false));
    }

    public BirthdayCritter asCritter() {
        return new BirthdayCritter(this.userId, this.time, this.year);
    }
}
