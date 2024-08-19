package com.github.milomarten.fracktail4.platform.discord.slash;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import reactor.core.publisher.Mono;

public interface SlashCommandFilter {
    Mono<Boolean> filter(ChatInputInteractionEvent event, SlashCommandFilterChain next);
}
