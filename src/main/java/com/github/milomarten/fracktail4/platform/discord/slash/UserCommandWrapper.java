package com.github.milomarten.fracktail4.platform.discord.slash;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface UserCommandWrapper {
    ApplicationCommandRequest getRequest();

    Mono<?> handleEvent(UserInteractionEvent event);
}
