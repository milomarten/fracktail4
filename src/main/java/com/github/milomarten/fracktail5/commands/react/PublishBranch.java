package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordNoArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;

public class PublishBranch {
    public static DiscordNoArgumentSubCommandDetails details(RoleReactCommand parent) {
        return new DiscordNoArgumentSubCommandDetails(
                "publish",
                "Publish the created/edited role-react for use",
                () -> publish(parent)
        );
    }

    public static DiscordResponse publish(RoleReactCommand parent) {
        return parent.removeContents(
                rr -> {
                    return DiscordResponses.delayedResponse(
                            parent.getReacts().publish(rr)
                                    .map(i -> String.format("Published! ID is %d", i))
                    );
                }
        );
    }
}
