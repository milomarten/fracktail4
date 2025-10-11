package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

/**
 * Represents that a command can be run as a Discord Slash Command.
 * The reason for this two-level approach is to allow a base Command class to support
 * as many different platforms as it wants to. For those who only plan on supporting
 * one platform, the Command may implement both interfaces.
 */
public interface DiscordSlashCommand {
    /**
     * Get the details on how this DiscordSlashCommand should operate
     * @return The details of this command
     * @see DiscordNoArgumentDetails
     * @see DiscordArgumentDetails
     */
    Details getDiscordSlashCommandDetails();

    interface Details {
        /**
         * Get the name of the command
         * Potential future enhancement: move this to a common Code class, since every
         * command will have a name, and should probably be the same regardless of platform.
         * @return The command name
         */
        String getName();

        /**
         * Get the spec of the command.
         * In this case, the spec is a VisitorGroup which modifies an empty ApplicationCommandRequest builder.
         * When constructing the command specification for Discord, all of these Visitors will be run through
         * to construct the final request to Discord.
         * @return The spec of the command.
         */
        VisitorGroup<ImmutableApplicationCommandRequest.Builder> getSpec();

        /**
         * The code to execute when running this command
         * @param event The slash event context
         * @return A response of some sort.
         * @see com.github.milomarten.fracktail5.platform.util.DiscordResponses
         */
        DiscordResponse invoke(ChatInputInteractionEvent event);
    }
}
