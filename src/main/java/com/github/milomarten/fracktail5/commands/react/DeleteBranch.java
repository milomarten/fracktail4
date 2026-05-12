package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.IntArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;

public class DeleteBranch {
    public static DiscordArgumentSubCommandDetails<Integer> details(RoleReactCommand parent) {
        return new DiscordArgumentSubCommandDetails<>(
                "delete",
                "Delete a Role React forever",
                new IntArgumentParser("id", "Role React ID").required(),
                i -> delete(parent, i)
        );
    }

    public static DiscordResponse delete(RoleReactCommand parent, int id) {
        return DiscordResponses.delayedResponse(parent.getReacts().deleteById(id)
                .thenReturn("Deleted role react!")
        );
    }
}
