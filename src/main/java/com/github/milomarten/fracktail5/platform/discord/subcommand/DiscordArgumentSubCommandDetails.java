package com.github.milomarten.fracktail5.platform.discord.subcommand;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.Argument;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;

import java.util.function.Function;

/**
 * One type of Discord command, which takes one or more arguments.
 * For this command, the context of the command is compiled into an object in some form.
 * This is left generic, in order to support one user-supplied parameter, one context-supplied
 * parameter (such as a user ID), or grouping some combination thereof into a record or POJO.
 * @param <ARG> The type used to combine all arguments together.
 */
public class DiscordArgumentSubCommandDetails<ARG> implements DiscordSubCommand.Details {
    @Getter private final String branchName;
    @Getter private final VisitorGroup<ImmutableApplicationCommandOptionData.Builder> branchSpec;
    private final Argument<ARG> argParser;
    private final Function<ARG, DiscordResponse> invoker;

    /**
     * Create these details
     * @param branchName The name of the command
     * @param description The description of the command
     * @param argParser The algorithm to create the Argument object
     * @param invoker The function to execute, using the arguments, when the command is invoked.
     * @see com.github.milomarten.fracktail5.platform.discord.argument.PojoParser
     */
    public DiscordArgumentSubCommandDetails(
            String branchName,
            String description,
            Argument<ARG> argParser,
            Function<ARG, DiscordResponse> invoker
    ) {
        this.branchName = branchName;
        this.argParser = argParser;
        this.invoker = invoker;
        this.branchSpec = new VisitorGroup<ImmutableApplicationCommandOptionData.Builder>()
                .add(DiscordVisitor.argName(branchName))
                .add(DiscordVisitor.argDescription(description))
                .add(input -> input.addAllOptions(argParser.getOptions()));
    }

    @Override
    public DiscordResponse invokeBranch(ChatInputInteractionEvent event, ApplicationCommandInteractionOption acio) {
        var args = argParser.get(event, acio);
        return this.invoker.apply(args);
    }
}
