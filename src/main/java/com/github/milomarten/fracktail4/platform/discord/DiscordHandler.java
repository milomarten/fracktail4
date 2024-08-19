package com.github.milomarten.fracktail4.platform.discord;

import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
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
