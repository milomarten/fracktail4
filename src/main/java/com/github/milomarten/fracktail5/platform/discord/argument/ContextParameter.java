package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;
import java.util.function.Function;

public record ContextParameter<T>(Function<ChatInputInteractionEvent, T> func) implements Argument<T> {
    @Override
    public List<ApplicationCommandOptionData> getOptions() {
        return List.of();
    }

    @Override
    public T get(ChatInputInteractionEvent event) {
        return func.apply(event);
    }
}
