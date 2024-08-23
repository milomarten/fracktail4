package com.github.milomarten.fracktail4.permissions.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

/**
 * Describe the different ways to get role qualifications.
 */
public enum DiscordKeyType {
    /**
     * The rule matches if the user has the value as their user ID.
     */
    ID {
        @Override
        public boolean matches(DiscordPermissionRule rule, User user) {
            return user.getId().equals(Snowflake.of(rule.getValue()));
        }
    },
    /**
     * The rule matches if the user has the value as one of their roles.
     * Note that this rule will return false if the User object is not a Member (i.e., the
     * command is being run in DMs). Consider another rule if you want to enforce this in DMs!
     */
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
