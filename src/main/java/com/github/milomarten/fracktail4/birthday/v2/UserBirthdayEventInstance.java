package com.github.milomarten.fracktail4.birthday.v2;

import com.github.milomarten.fracktail4.birthday.BirthdayCritter;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Dur;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.MonthDay;
import java.time.Year;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class UserBirthdayEventInstance implements BirthdayEventInstance {

    private final MonthDay time;
    private final Year year;
    private final Snowflake userId;
    private final GatewayDiscordClient client;

    @Getter(lazy = true) private final Mono<String> name = Mono.defer(this::_getName).cache(Duration.ofMinutes(5));
    private final Map<Snowflake, Mono<Boolean>> guildCache = new ConcurrentHashMap<>();

    public Snowflake userId() {
        return userId;
    }

    public MonthDay time() {
        return time;
    }

    public Mono<String> _getName() {
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
        return this.guildCache.computeIfAbsent(guildId, id -> {
            return Mono.defer(() -> _shouldDisplayForGuild(guildId)).cache(Duration.ofMinutes(10));
        });
    }

    private Mono<Boolean> _shouldDisplayForGuild(Snowflake guildId) {
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
