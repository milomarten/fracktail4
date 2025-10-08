package com.github.milomarten.fracktail4.platform.discord;

import com.github.milomarten.fracktail.core.FracktailVersion;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

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
    @ConditionalOnProperty(value = "discord.startupAnnouncement.enabled", havingValue = "true")
    public ApplicationListener<ApplicationReadyEvent> onReadyDiscord(
            GatewayDiscordClient client,
            @Value("${discord.startupAnnouncement.timezone:UTC}") ZoneId timezone,
            @Value("${discord.ownerId}") Snowflake ownerId
    ) {
        return event -> client.getUserById(ownerId)
                .flatMap(User::getPrivateChannel)
                .flatMap(pc -> {
                    ZonedDateTime zdt = ZonedDateTime.now(timezone);
                    return pc.createMessage("Good morning! It is " + DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).format(zdt) + ", and I am ready to serve.");
                })
                .subscribe((obj) -> {}, (err) -> {}, () -> {});
    }

    @Bean
    public ApplicationListener<ApplicationReadyEvent> onReadyDiscordDescriptionSetup(GatewayDiscordClient client) {
        return event -> {
            String version = FracktailVersion.getVersion();
            if (StringUtils.isNotBlank(version)) {
                client.updatePresence(ClientPresence.online(ClientActivity.playing(version))).subscribe();
            }
        };
    }
}
