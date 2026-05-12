package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail.core.discord.react.ReactMessage;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.IntArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;

public class EditBranch {
    public static DiscordArgumentSubCommandDetails<Integer> details(RoleReactCommand parent) {
        return new DiscordArgumentSubCommandDetails<>(
                "create",
                "Create a new Role React set",
                new IntArgumentParser("id", "ID of role-react to edit").required(),
                params -> edit(parent, params)
        );
    }

    public static DiscordResponse edit(RoleReactCommand parent, int id) {
        var find = parent.getReacts().getById(id);
        if (find.isEmpty()) {
            return DiscordResponses.replyEphemeral("role-react was not found.");
        }

        return parent.createContents(
                () -> new ReactMessage<>(find.get()),
                DiscordResponses.replyEphemeral("Editing role-react. Add more options and use /role-react publish to complete.")
        );
    }
}
