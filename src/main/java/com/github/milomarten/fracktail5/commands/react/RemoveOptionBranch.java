package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.RoleArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;

import java.util.Objects;

public class RemoveOptionBranch {
    public static DiscordArgumentSubCommandDetails<Snowflake> details(RoleReactCommand parent) {
        return new DiscordArgumentSubCommandDetails<>(
                "remove-option",
                "Remove an option from a role-react",
                new RoleArgumentParser("role", "The role to remove").required(),
                params -> removeOption(parent, params)
        );
    }

    private static DiscordResponse removeOption(RoleReactCommand parent, Snowflake param) {
        return parent.updateContents(rm -> {
            var found = rm.getOptions()
                    .removeIf(opt -> Objects.equals(opt.getId(), param));
            if (found) {
                return DiscordResponses.replyEphemeral("Removed option");
            } else {
                return DiscordResponses.replyEphemeral("Option not found");
            }
        });
    }
}
