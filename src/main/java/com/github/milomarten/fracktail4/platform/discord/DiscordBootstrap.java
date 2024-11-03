package com.github.milomarten.fracktail4.platform.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    @Bean
    public ApplicationListener<ApplicationReadyEvent> onReadyDiscord(GatewayDiscordClient client) {
        return event -> client.getUserById(Snowflake.of(248612704019808258L))
                .flatMap(User::getPrivateChannel)
                .flatMap(pc -> {
                    LocalDateTime ldt = LocalDateTime.now();
                    return pc.createMessage("Good morning! It is " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(ldt) + ", and I am ready to serve.");
                })
                .subscribe((obj) -> {}, (err) -> {}, () -> {});
    }
}
