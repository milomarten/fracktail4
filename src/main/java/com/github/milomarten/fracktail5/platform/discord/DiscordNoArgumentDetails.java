package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.argument.DiscordArgumentParser;
import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;

import java.util.function.Function;
import java.util.function.Supplier;

public class DiscordNoArgumentDetails implements DiscordSlashCommand.Details {
    @Getter private final String name;
    @Getter private final VisitorGroup<ImmutableApplicationCommandRequest.Builder> spec;
    private final Supplier<DiscordResponse> invoker;

    public DiscordNoArgumentDetails(
            String name,
            String description,
            Supplier<DiscordResponse> invoker
    ) {
        this.name = name;
        this.invoker = invoker;
        this.spec = new VisitorGroup<ImmutableApplicationCommandRequest.Builder>()
                .add(DiscordVisitor.name(name))
                .add(DiscordVisitor.description(description));
    }

    @Override
    public final DiscordResponse invoke(ChatInputInteractionEvent event) {
        return this.invoker.get();
    }
}
