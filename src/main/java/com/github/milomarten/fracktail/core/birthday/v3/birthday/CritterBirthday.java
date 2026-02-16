package com.github.milomarten.fracktail.core.birthday.v3.birthday;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

import java.time.Year;
import java.util.Optional;

public interface CritterBirthday {
    Mono<Boolean> isValidForServer(GatewayDiscordClient client, Snowflake serverId);
    String displayName();
    Optional<Year> displayYearOfBirth();
}
