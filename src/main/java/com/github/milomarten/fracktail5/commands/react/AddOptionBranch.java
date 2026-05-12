package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail.core.discord.react.EmojiUtil;
import com.github.milomarten.fracktail.core.discord.react.ReactOption;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.RoleArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Role;
import lombok.Data;
import reactor.core.publisher.Mono;

public class AddOptionBranch {
    public static DiscordArgumentSubCommandDetails<Params> details(RoleReactCommand parent) {
        return new DiscordArgumentSubCommandDetails<>(
                "add-option",
                "Add an option to a role-react",
                new PojoParser<>(Params::new)
                        .addField(
                                new StringArgumentParser("emoji", "The emoji to grant this role").required(),
                                Params::setEmoji
                        )
                        .addField(
                                new RoleArgumentParser("role", "The role to grant").asRole(true),
                                Params::setRole
                        )
                ,
                params -> addOption(parent, params)
        );
    }

    private static DiscordResponse addOption(RoleReactCommand parent, Params params) {
        return parent.updateContents(rm -> {
            return DiscordResponses.delayedResponse(
                    params.toOption()
                            .map(ro -> {
                                var duplicate = rm.getOptions().stream()
                                        .anyMatch(ro::equalsByIdOrReaction);
                                if (duplicate) {
                                    return "Emoji or role is already present!";
                                } else {
                                    rm.getOptions().add(ro);
                                    return "Added role!";
                                }
                            })
            );
        });
    }

    @Data
    public static class Params {
        Mono<Role> role;
        String emoji;

        public Mono<ReactOption<Snowflake>> toOption() {
            return role.map(r -> {
                return new ReactOption<>(
                        r.getId(),
                        EmojiUtil.toEmoji(emoji),
                        r.getName()
                );
            });
        }
    }
}
