package com.github.milomarten.fracktail.core.job;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AbstractAnnouncementJob {
    @Setter @Autowired private GatewayDiscordClient discordClient;

    @Getter private final Snowflake announcementChannelId;
    @Getter private TextChannel announcementChannel;

    @PostConstruct
    private void setUp() {
        var aChannelMaybe = discordClient.getChannelById(announcementChannelId)
                .cast(TextChannel.class)
                .blockOptional();
        if (aChannelMaybe.isPresent()) {
            this.announcementChannel = aChannelMaybe.get();
        } else {
            log.error("Couldn't pull text channel {}", announcementChannelId);
        }
    }

    protected void sendAnnouncement(String message) {
        this.announcementChannel.createMessage(message)
                .subscribe(
                        null,
                        ex -> log.error("Error announcing message", ex)
                );
    }
}
