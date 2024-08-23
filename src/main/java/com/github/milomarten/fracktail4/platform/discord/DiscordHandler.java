package com.github.milomarten.fracktail4.platform.discord;

import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Performs logic to be done after the Discord client is spun up.
 * Right now, this just pulls all DiscordHookSources, and calls their setup method.
 * This is only spun up if a Discord Client exists.
 */
@Component
@ConditionalOnBean(GatewayDiscordClient.class)
public class DiscordHandler {
    private final GatewayDiscordClient gateway;

    public DiscordHandler(GatewayDiscordClient gateway, List<DiscordHookSource> visitors) {
        this.gateway = gateway;
        for (DiscordHookSource visitor : visitors) {
            visitor.addDiscordHook(this.gateway);
        }
    }

    @PreDestroy
    public void onDestroy() {
        gateway.logout().block();
    }
}
