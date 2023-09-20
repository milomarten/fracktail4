package com.github.milomarten.fracktail4.hook;

import discord4j.core.GatewayDiscordClient;

public interface DiscordHookSource {
    void addDiscordHook(GatewayDiscordClient client);
}
