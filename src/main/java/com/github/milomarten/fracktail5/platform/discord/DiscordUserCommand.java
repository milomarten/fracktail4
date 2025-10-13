package com.github.milomarten.fracktail5.platform.discord;

import discord4j.core.event.domain.interaction.UserInteractionEvent;

public interface DiscordUserCommand {
    Details getDiscordUserCommandDetails();

    interface Details {
        String getName();
        DiscordUserResponse invoke(UserInteractionEvent event);
    }
}
