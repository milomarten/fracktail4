package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.permissions.PermissionProvider;
import discord4j.core.object.entity.User;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Configuration
@ConfigurationProperties("permissions.discord")
public class DiscordPermissionProvider implements PermissionProvider<User, DiscordRole> {
    private List<DiscordPermissionRule> rules;

    @Override
    public Set<DiscordRole> getPermissionsForUser(User user) {
        return rules.stream()
                .<DiscordRole>mapMulti((dpr, consumer) -> {
                    if (dpr.getKey().matches(dpr, user)) {
                        consumer.accept(dpr.getRole());
                    }
                })
                .collect(Collectors.toSet());
    }
}
