package com.github.milomarten.fracktail4.config;

import com.github.milomarten.fracktail4.permissions.PermissionsProvider;
import com.github.milomarten.fracktail4.permissions.RoleRule;
import com.github.milomarten.fracktail4.permissions.RuleBasedPermissionProvider;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;

@Configuration
public class PermissionsConfig {
    @Value("${discord.ownerId}") private Snowflake ownerId;
    @Value("${discord.modRoleId}") private Snowflake modRoleId;

    @Bean
    public PermissionsProvider<User, FracktailRoles> permissionsProvider() {
        return RuleBasedPermissionProvider.<User, FracktailRoles>builder()
                .rules(rules())
                .finisher(() -> EnumSet.noneOf(FracktailRoles.class))
                .build();
    }

    private List<RoleRule<User, FracktailRoles>> rules() {
        return List.of(
                new RoleRule<>(FracktailRoles.OWNER, FracktailRoleMatcher.BY_USER_ID.curry(ownerId)),
                new RoleRule<>(FracktailRoles.MOD, FracktailRoleMatcher.BY_ROLE_ID.curry(modRoleId))
        );
    }
}
