package com.github.milomarten.fracktail.core.job;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "discord.moon.enabled", havingValue = "true")
public class MoonJob extends AbstractAnnouncementJob {
    public MoonJob(@Value("${discord.moon.announcementChannelId}") Snowflake announcementChannelId) {
        super(announcementChannelId);
    }

    public static final String HOME_TIMEZONE_RAW = "America/New_York";
    public static final ZoneId HOME_TIMEZONE = ZoneId.of(HOME_TIMEZONE_RAW);

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceMoon() {
        var nowInIslamic = HijrahDate.now(HOME_TIMEZONE);

        var daysInThisMonth = nowInIslamic.lengthOfMonth();

        if (nowInIslamic.get(ChronoField.DAY_OF_MONTH) == daysInThisMonth / 2) {
            sendAnnouncement("\uD83C\uDF15 \uD83D\uDC3A");
        }
    }
}
