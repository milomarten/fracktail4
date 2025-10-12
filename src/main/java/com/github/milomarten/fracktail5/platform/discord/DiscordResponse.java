package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

/**
 * A generic response to a DiscordSlashCommand.
 * The response can do anything, asynchronous or otherwise.
 * @see DiscordResponses
 */
public interface DiscordResponse {
    Mono<?> respondTo(ChatInputInteractionEvent event);
}
