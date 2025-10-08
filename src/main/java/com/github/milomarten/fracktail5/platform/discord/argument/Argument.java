package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;

public interface Argument<TYPE> {
    List<ApplicationCommandOptionData> getOptions();
    TYPE get(ChatInputInteractionEvent event);
}
