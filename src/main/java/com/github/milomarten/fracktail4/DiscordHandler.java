package com.github.milomarten.fracktail4;

import com.github.milomarten.fracktail4.hook.GatewayVisitor;
import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DiscordHandler {
    private final GatewayDiscordClient gateway;

    public DiscordHandler(GatewayDiscordClient gateway, List<GatewayVisitor> visitors) {
        this.gateway = gateway;
        for (GatewayVisitor visitor : visitors) {
            visitor.addHook(this.gateway);
        }
    }

    @PreDestroy
    public void onDestroy() {
        gateway.logout().block();
    }
}
