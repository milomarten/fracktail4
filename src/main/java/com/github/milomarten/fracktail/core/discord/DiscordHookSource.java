package com.github.milomarten.fracktail.core.discord;

import discord4j.core.GatewayDiscordClient;

public interface DiscordHookSource {
    void addDiscordHook(GatewayDiscordClient client);
}
