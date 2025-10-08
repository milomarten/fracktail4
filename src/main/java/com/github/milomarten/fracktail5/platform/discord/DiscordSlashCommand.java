package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

public interface DiscordSlashCommand {
    Details getDiscordSlashCommandDetails();

    interface Details {
        String getName();
        VisitorGroup<ImmutableApplicationCommandRequest.Builder> getSpec();
        DiscordResponse invoke(ChatInputInteractionEvent event);
    }
}
