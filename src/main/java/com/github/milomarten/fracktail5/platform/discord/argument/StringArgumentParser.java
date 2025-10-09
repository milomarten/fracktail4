package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;

import java.util.Optional;

public class StringArgumentParser extends BaseOptionalArgumentParser<String> {
    public StringArgumentParser(String name, String description) {
        super(name, description);
    }

    @Override
    public Optional<String> convert(ChatInputInteractionEvent chatInputInteractionEvent) {
        return chatInputInteractionEvent.getOption(this.name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);
    }

    public StringArgumentParser minLength(int minLength) {
        addVisitor(DiscordVisitor.argMinLength(minLength));
        return this;
    }

    public StringArgumentParser maxLength(int maxLength) {
        addVisitor(DiscordVisitor.argMaxLength(maxLength));
        return this;
    }
}
