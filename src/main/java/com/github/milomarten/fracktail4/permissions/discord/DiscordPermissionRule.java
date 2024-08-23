package com.github.milomarten.fracktail4.permissions.discord;

import lombok.Data;

@Data
public class DiscordPermissionRule {
    private DiscordRole role;
    private DiscordKeyType key;
    private String value;
}
