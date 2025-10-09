package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;

import java.util.Optional;

public class BooleanArgumentParser extends BaseOptionalArgumentParser<Boolean> {
    public BooleanArgumentParser(String name, String description) {
        super(name, description);
    }

    @Override
    public Optional<Boolean> convert(ChatInputInteractionEvent chatInputInteractionEvent) {
        return chatInputInteractionEvent.getOption(this.name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean);
    }
}
