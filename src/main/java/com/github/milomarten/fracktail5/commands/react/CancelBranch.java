package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordNoArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;

public class CancelBranch {
    public static DiscordNoArgumentSubCommandDetails details(RoleReactCommand parent) {
        return new DiscordNoArgumentSubCommandDetails(
                "cancel",
                "Cancel the modified role-react and discard changes",
                () -> cancel(parent)
        );
    }

    public static DiscordResponse cancel(RoleReactCommand parent) {
        return parent.removeContents(
                rr -> DiscordResponses.replyEphemeral("Canceled! role-react was discarded.")
        );
    }
}
