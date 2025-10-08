package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.ParameterConverter;
import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

public interface DiscordArgumentParser<OUT>
        extends ParameterConverter<ChatInputInteractionEvent, OUT>,
        Visitor<ImmutableApplicationCommandRequest.Builder> {
}
