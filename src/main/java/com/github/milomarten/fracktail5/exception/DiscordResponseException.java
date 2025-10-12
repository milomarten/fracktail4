package com.github.milomarten.fracktail5.exception;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public class DiscordResponseException extends RuntimeException implements DiscordResponse {
    public DiscordResponseException(String message) {
        super(message);
    }

    @Override
    public Mono<?> respondTo(ChatInputInteractionEvent event) {
        return event.reply(getMessage());
    }
}
