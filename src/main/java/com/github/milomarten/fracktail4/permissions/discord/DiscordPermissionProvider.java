package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.permissions.PermissionProvider;
import com.github.milomarten.fracktail4.permissions.Permissions;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties("discord.permissions")
public class DiscordPermissionProvider implements PermissionProvider<User, DiscordRole> {
    private List<DiscordPermissionRule> rules;

    @Override
    public Permissions<DiscordRole> getPermissionsForUser(User user) {
        var roleList = rules.stream()
                .<DiscordRole>mapMulti((dpr, consumer) -> {
                    if (dpr.getKey().matches(dpr, user)) {
                        consumer.accept(dpr.getRole());
                    }
                })
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DiscordRole.class)));
        return new Permissions<>(roleList);
    }
}
