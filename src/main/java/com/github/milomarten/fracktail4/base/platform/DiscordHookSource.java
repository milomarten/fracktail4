package com.github.milomarten.fracktail4.base.platform;

import discord4j.core.GatewayDiscordClient;

public interface DiscordHookSource {
    void addDiscordHook(GatewayDiscordClient client);
}
