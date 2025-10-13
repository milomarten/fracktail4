package com.github.milomarten.fracktail5.platform.discord;

import discord4j.core.event.domain.interaction.UserInteractionEvent;
import reactor.core.publisher.Mono;

public interface DiscordUserResponse {
    Mono<?> respondTo(UserInteractionEvent event);

    static DiscordUserResponse replyEphemeral(String message) {
        return event -> event.reply(message).withEphemeral(true);
    }
}
