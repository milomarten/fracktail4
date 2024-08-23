package com.github.milomarten.fracktail4.platform.discord.slash;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import reactor.core.publisher.Mono;

/**
 * Marks a bean as a slash command
 */
public interface SlashCommandWrapper {
    /**
     * The Discord definition of this command. This describes the name, description, parameters, permissions...
     * @return The request to send to Discord.
     */
    ApplicationCommandRequest getRequest();

    /**
     * Perform the logic of the slash command.
     * Note that this requires total handling of the lifecycle, including validation, deferring replies, and such.
     * There are Abstract Classes which can be used to automate some portions of this logic.
     * @param event The triggering event
     * @return A Mono which indicates the response was complete.
     */
    Mono<?> handleEvent(ChatInputInteractionEvent event);
}
