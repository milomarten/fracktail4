package com.github.milomarten.fracktail5.platform.discord.util;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

@UtilityClass
public class DiscordResponses {
    /**
     * Reply to the command usage with a simple text response
     * Note that this expects a near-instant response (<3 seconds). For longer responses,
     * use defer()
     * @param message The message to reply with
     * @return A DiscordResponse which replies in that way
     */
    public DiscordResponse reply(String message) {
        return event -> event.reply(message);
    }

    /**
     * Reply ephemerally to the command usage with a simple text response
     * Note that this expects a near-instant response (<3 seconds). For longer responses,
     * use defer()
     * @param message The message to reply with
     * @return A DiscordResponse which replies in that way
     */
    public DiscordResponse replyEphemeral(String message) {
        return event -> event.reply(message).withEphemeral(true);
    }

    /**
     * Respond in a certain way after some amount of time goes by.
     * This can be used for slower responses, that may call a backend. While the bot is
     * "thinking", a message will display automatically for the command user to indicate that.
     * All messages are visible, so all viewers can see it
     * @param later A Mono which, when finished, has the content to display
     * @return A DiscordResponse which invokes the later response when it is ready.
     */
    public DiscordResponse delayedResponse(Mono<String> later) {
        return event ->
                event.deferReply()
                        .then(later)
                        .flatMap(event::createFollowup);
    }

    /**
     * Respond in a certain way after some amount of time goes by.
     * This can be used for slower responses, that may call a backend. While the bot is
     * "thinking", a message will display automatically for the command user to indicate that.
     * All messages are ephemeral, meaning they are only visible on the user's device.
     * @param later A Mono which, when finished, has the content to display
     * @return A DiscordResponse which invokes the later response when it is ready.
     */
    public DiscordResponse delayedEphemeralResponse(Mono<String> later) {
        return event ->
                event.deferReply()
                        .withEphemeral(true)
                        .then(later)
                        .flatMap(event::createFollowup);
    }
}
