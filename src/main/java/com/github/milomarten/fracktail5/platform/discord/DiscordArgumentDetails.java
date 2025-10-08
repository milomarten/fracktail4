package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.argument.DiscordArgumentParser;
import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;

import java.util.function.Function;

public class DiscordArgumentDetails<ARG> implements DiscordSlashCommand.Details {
    @Getter private final String name;
    @Getter private final VisitorGroup<ImmutableApplicationCommandRequest.Builder> spec;
    private final DiscordArgumentParser<ARG> argParser;
    private final Function<ARG, DiscordResponse> invoker;

    public DiscordArgumentDetails(
            String name,
            String description,
            DiscordArgumentParser<ARG> argParser,
            Function<ARG, DiscordResponse> invoker
    ) {
        this.name = name;
        this.argParser = argParser;
        this.invoker = invoker;
        this.spec = new VisitorGroup<ImmutableApplicationCommandRequest.Builder>()
                .add(DiscordVisitor.name(name))
                .add(DiscordVisitor.description(description))
                .add(argParser);
    }

    @Override
    public final DiscordResponse invoke(ChatInputInteractionEvent event) {
        var args = argParser.convert(event);
        return this.invoker.apply(args);
    }
}
