package com.github.milomarten.fracktail4.permissions.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

public enum DiscordKeyType {
    ID {
        @Override
        public boolean matches(DiscordPermissionRule rule, User user) {
            return user.getId().equals(Snowflake.of(rule.getValue()));
        }
    },
    CONTAINS_ROLE {
        @Override
        public boolean matches(DiscordPermissionRule rule, User user) {
            if (user instanceof Member member) {
                return member.getRoleIds().contains(Snowflake.of(rule.getValue()));
            }
            return false;
        }
    }
    ;

    public abstract boolean matches(DiscordPermissionRule rule, User user);
}
