package com.github.milomarten.fracktail5.platform.discord;

import discord4j.core.GatewayDiscordClient;

public interface DiscordHookSource {
    void addDiscordHook(GatewayDiscordClient client);
}
