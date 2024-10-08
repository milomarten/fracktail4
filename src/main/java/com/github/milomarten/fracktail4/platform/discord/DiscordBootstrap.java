package com.github.milomarten.fracktail4.platform.discord;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "discord", name = "token")
public class DiscordBootstrap {
    @Bean
    public DiscordClient discordClient(@Value("${discord.token}") String token) {
        return DiscordClient.create(token);
    }

    @Bean
    public GatewayDiscordClient gatewayDiscordClient(DiscordClient discordClient) {
        return discordClient.login().block();
    }
}
