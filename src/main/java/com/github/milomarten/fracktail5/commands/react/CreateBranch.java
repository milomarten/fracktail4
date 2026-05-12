package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail.core.discord.react.ReactMessage;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.ContextParameter;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;

public class CreateBranch {
    public static DiscordArgumentSubCommandDetails<ReactMessage<Snowflake>> details(RoleReactCommand parent) {
        return new DiscordArgumentSubCommandDetails<>(
                "create",
                "Create a new Role React set",
                new PojoParser<ReactMessage<Snowflake>>(ReactMessage::new)
                        .addField(
                                new ContextParameter<>(cie -> cie.getInteraction().getGuildId().orElseThrow()),
                                ReactMessage::setGuildId
                        )
                        .addField(
                                new StringArgumentParser("description", "Description of what the purpose of the react is for.")
                                        .defaultToEmpty(),
                                ReactMessage::setDescription
                        )
                        .addField(
                                new ContextParameter<>(cie -> cie.getInteraction().getChannelId()),
                                ReactMessage::setChannelId
                        )
                ,
                params -> create(parent, params)
        );
    }

    public static DiscordResponse create(RoleReactCommand parent, ReactMessage<Snowflake> params) {
        return parent.createContents(
                () -> params,
                DiscordResponses.replyEphemeral("Initialized a new role-react. Add more options and use /role-react publish to complete.")
        );
    }
}
