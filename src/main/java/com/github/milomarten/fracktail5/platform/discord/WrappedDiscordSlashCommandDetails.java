package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

public abstract class WrappedDiscordSlashCommandDetails implements DiscordSlashCommand, DiscordSlashCommand.Details {
    private final DiscordSlashCommand.Details wrapped;

    public WrappedDiscordSlashCommandDetails(DiscordSlashCommand wrapped) {
        this.wrapped = wrapped.getDiscordSlashCommandDetails();
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public VisitorGroup<ImmutableApplicationCommandRequest.Builder> getSpec() {
        return wrapped.getSpec();
    }

    protected DiscordResponse invokeWrapped(ChatInputInteractionEvent event) {
        return wrapped.invoke(event);
    }

    @Override
    public Details getDiscordSlashCommandDetails() {
        return this;
    }
}
