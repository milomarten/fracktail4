package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;

import java.util.Optional;

public class IntArgumentParser extends BaseOptionalArgumentParser<Integer> {
    public IntArgumentParser(String name, String description) {
        super(name, description);
    }

    @Override
    public Optional<Integer> get(ChatInputInteractionEvent event) {
        return event.getOption(this.name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asLong)
                .map(Long::intValue);
    }

    public IntArgumentParser min(int min) {
        this.addVisitor(DiscordVisitor.argMin(min));
        return this;
    }

    public IntArgumentParser max(int max) {
        this.addVisitor(DiscordVisitor.argMax(max));
        return this;
    }
}
