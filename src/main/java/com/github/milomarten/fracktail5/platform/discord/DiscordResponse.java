package com.github.milomarten.fracktail5.platform.discord;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface DiscordResponse {
    Mono<?> respondTo(ChatInputInteractionEvent event);
}
