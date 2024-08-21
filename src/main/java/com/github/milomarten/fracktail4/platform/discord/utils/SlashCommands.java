package com.github.milomarten.fracktail4.platform.discord.utils;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;

public class SlashCommands {
    public static Mono<Void> replyEphemeral(ApplicationCommandInteractionEvent event, String content) {
        return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .ephemeral(true)
                        .content(content)
                .build());
    }

    public static Mono<Void> followupEphemeral(ApplicationCommandInteractionEvent event, String content) {
        return event.createFollowup(InteractionFollowupCreateSpec.builder()
                        .ephemeral(true)
                        .content(content)
                .build()).then();
    }
}
