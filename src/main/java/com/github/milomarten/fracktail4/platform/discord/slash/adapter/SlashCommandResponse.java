package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface SlashCommandResponse {
    Mono<?> respond(ChatInputInteractionEvent event);
}
