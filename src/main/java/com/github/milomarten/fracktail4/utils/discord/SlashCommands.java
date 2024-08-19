package com.github.milomarten.fracktail4.utils.discord;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import reactor.core.publisher.Mono;

public class SlashCommands {
    public static Mono<Void> replyEphemeral(ApplicationCommandInteractionEvent event, String content) {
        return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .ephemeral(true)
                        .content(content)
                .build());
    }
}
