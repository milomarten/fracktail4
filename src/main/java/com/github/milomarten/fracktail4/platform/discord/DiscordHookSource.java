package com.github.milomarten.fracktail4.platform.discord;

import discord4j.core.GatewayDiscordClient;

/**
 * Marks this class as a source for Discord hooks.
 * The system will call each DiscordHookSource after the Discord Client is created, in order to attach
 * event handlers, or "hooks".
 */
public interface DiscordHookSource {
    void addDiscordHook(GatewayDiscordClient client);
}
