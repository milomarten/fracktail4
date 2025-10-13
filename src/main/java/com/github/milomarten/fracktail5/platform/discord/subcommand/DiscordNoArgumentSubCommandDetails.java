package com.github.milomarten.fracktail5.platform.discord.subcommand;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;

import java.util.function.Supplier;

/**
 * One type of Discord command, which takes no arguments at all.
 */
public class DiscordNoArgumentSubCommandDetails implements DiscordSubCommand.Details {
    @Getter private final String branchName;
    @Getter private final VisitorGroup<ImmutableApplicationCommandOptionData.Builder> branchSpec;
    private final Supplier<DiscordResponse> invoker;

    /**
     * Create these details
     * @param branchName The name of the command
     * @param description The description of the command
     * @param invoker The function to execute when the command is invoked.
     */
    public DiscordNoArgumentSubCommandDetails (
            String branchName,
            String description,
            Supplier<DiscordResponse> invoker
    ) {
        this.branchName = branchName;
        this.invoker = invoker;
        this.branchSpec = new VisitorGroup<ImmutableApplicationCommandOptionData.Builder>()
                .add(DiscordVisitor.argName(branchName))
                .add(DiscordVisitor.argDescription(description));
    }

    @Override
    public DiscordResponse invokeBranch(ChatInputInteractionEvent event, ApplicationCommandInteractionOption acio) {
        return this.invoker.get();
    }
}
