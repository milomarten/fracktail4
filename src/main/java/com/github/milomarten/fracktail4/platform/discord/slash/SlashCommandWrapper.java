package com.github.milomarten.fracktail4.platform.discord.slash;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

public interface SlashCommandWrapper {
    ApplicationCommandRequest getRequest();

    Mono<?> handleEvent(ChatInputInteractionEvent event);
}
