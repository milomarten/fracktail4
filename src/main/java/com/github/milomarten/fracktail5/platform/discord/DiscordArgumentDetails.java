package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.Visitor;
import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.argument.Argument;
import com.github.milomarten.fracktail5.platform.discord.argument.BaseArgumentParser;
import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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
public class DiscordArgumentDetails<ARG> implements DiscordSlashCommand.Details {
    @Getter private final String name;
    @Getter private final VisitorGroup<ImmutableApplicationCommandRequest.Builder> spec;
    private final Argument<ARG> argParser;
    private final Function<ARG, DiscordResponse> invoker;

    /**
     * Create these details
     * @param name The name of the command
     * @param description The description of the command
     * @param argParser The algorithm to create the Argument object
     * @param invoker The function to execute, using the arguments, when the command is invoked.
     * @see com.github.milomarten.fracktail5.platform.discord.argument.PojoParser
     */
    public DiscordArgumentDetails(
            String name,
            String description,
            Argument<ARG> argParser,
            Function<ARG, DiscordResponse> invoker
    ) {
        this.name = name;
        this.argParser = argParser;
        this.invoker = invoker;
        this.spec = new VisitorGroup<ImmutableApplicationCommandRequest.Builder>()
                .add(DiscordVisitor.name(name))
                .add(DiscordVisitor.description(description))
                .add(input -> input.addAllOptions(argParser.getOptions()));
    }

    @Override
    public final DiscordResponse invoke(ChatInputInteractionEvent event) {
        var args = argParser.get(event);
        return this.invoker.apply(args);
    }
}
