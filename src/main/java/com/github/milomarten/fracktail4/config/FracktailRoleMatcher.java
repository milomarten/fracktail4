package com.github.milomarten.fracktail4.config;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

import java.util.function.Predicate;

public enum FracktailRoleMatcher {
    BY_USER_ID {
        @Override
        public boolean test(User user, Snowflake value) {
            return user.getId().equals(value);
        }
    },
    BY_ROLE_ID {
        @Override
        public boolean test(User user, Snowflake value) {
            if (user instanceof Member member) {
                return member.getRoleIds().contains(value);
            }
            return false;
        }
    };

    public abstract boolean test(User user, Snowflake value);

    public Predicate<User> curry(Snowflake value) {
        return (user) -> test(user, value);
    }
}
