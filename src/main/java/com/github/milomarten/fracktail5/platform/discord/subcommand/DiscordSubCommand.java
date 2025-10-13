package com.github.milomarten.fracktail5.platform.discord.subcommand;

import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class DiscordSubCommand implements DiscordSlashCommand.Details {
    @Getter private final String name;
    @Getter private final VisitorGroup<ImmutableApplicationCommandRequest.Builder> spec;
    private final Map<String, Details> branches = new HashMap<>();

    public DiscordSubCommand(String name, String description) {
        this.name = name;
        this.spec = new VisitorGroup<ImmutableApplicationCommandRequest.Builder>()
                .add(DiscordVisitor.name(name))
                .add(DiscordVisitor.description(description));
    }

    public DiscordSubCommand addBranch(Details details) {
        this.branches.put(details.getBranchName(), details);
        this.spec.add(b -> {
            var seed = ApplicationCommandOptionData.builder()
                    .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue());
            var fruit = details.getBranchSpec().visit(seed);
            return b.addOption(fruit.build());
        });
        return this;
    }

    @Override
    public DiscordResponse invoke(ChatInputInteractionEvent event) {
        var usedOption = event.getOptions().get(0);

        var branch = branches.get(usedOption.getName());
        if (branch == null) {
            return DiscordResponses.replyEphemeral("Subcommand " + usedOption.getName() + " is sunset or under maintenance.");
        } else {
            return branch.invokeBranch(event, usedOption);
        }
    }

    public interface Details {
        String getBranchName();
        VisitorGroup<ImmutableApplicationCommandOptionData.Builder> getBranchSpec();
        DiscordResponse invokeBranch(ChatInputInteractionEvent event, ApplicationCommandInteractionOption acio);
    }
}
