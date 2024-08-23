package com.github.milomarten.fracktail4.platform.discord.utils;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.InteractionFollowupCreateSpec;
import reactor.core.publisher.Mono;

/**
 * Generic ways to respond to slash commands.
 */
public class SlashCommands {
    /**
     * Reply to this event ephemerally.
     * For my notes: "ephemerally" means it only shows up for the sender. Good for error messages or other secret commands.
     * This doesn't work if you defer reply. use followupEphemeral, if that is your intent.
     * @param event The event to reply to
     * @param content The content of the reply
     * @return A Mono which completes when the response is sent.
     */
    public static Mono<Void> replyEphemeral(ApplicationCommandInteractionEvent event, String content) {
        return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .ephemeral(true)
                        .content(content)
                .build());
    }

    /**
     * Reply to this event ephemerally.
     * This works only if you've deferred reply (I have not tried using this without a deferred reply)
     * @param event The event to reply to
     * @param content The content of the reply
     * @return A Mono which completes when the response is sent.
     */
    public static Mono<Void> followupEphemeral(ApplicationCommandInteractionEvent event, String content) {
        return event.createFollowup(InteractionFollowupCreateSpec.builder()
                        .ephemeral(true)
                        .content(content)
                .build()).then();
    }

    /**
     * Reply to this event non-ephemerally.
     * @param event The event to reply to
     * @param content The content of the reply
     * @return A Mono which completes when the response is sent.
     */
    public static Mono<Void> followup(ApplicationCommandInteractionEvent event, String content) {
        return event.createFollowup(InteractionFollowupCreateSpec.builder()
                .content(content)
                .build()).then();
    }
}
