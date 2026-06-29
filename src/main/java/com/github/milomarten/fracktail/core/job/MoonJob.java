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

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.chrono.HijrahDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.Map;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "discord.moon.enabled", havingValue = "true")
public class MoonJob extends AbstractAnnouncementJob {
    public MoonJob(@Value("${discord.moon.announcementChannelId}") Snowflake announcementChannelId) {
        super(announcementChannelId);
    }

    public static final String HOME_TIMEZONE_RAW = "America/New_York";
    public static final ZoneId HOME_TIMEZONE = ZoneId.of(HOME_TIMEZONE_RAW);

    private static final String[] MOON_NAMES = {
            "", // Sentinel...no Month 0
            "Wolf", "Snow", "Worm", "Pink", "Flower", "Strawberry",
            "Buck", "Sturgeon", "Harvest", "Hunter's", "Beaver", "Cold"
    };

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceMoon() {
        var nowInIslamic = HijrahDate.now(HOME_TIMEZONE);
        var nowInUS = LocalDate.now(HOME_TIMEZONE);

        var daysInThisMonth = nowInIslamic.lengthOfMonth();

        if (nowInIslamic.get(ChronoField.DAY_OF_MONTH) == daysInThisMonth / 2) {
            if (isBlueMoon(nowInIslamic, nowInUS)) {
                sendAnnouncement("\uD83D\uDD35", "Blue");
            } else {
                sendAnnouncement("\uD83C\uDF15", MOON_NAMES[nowInUS.getMonthValue()]);
            }
        }
    }

    private boolean isBlueMoon(HijrahDate todayInHijrah, LocalDate todayInUS) {
        var oneMonthAgo = todayInHijrah.minus(1, ChronoUnit.MONTHS);
        var lastFullMoon = oneMonthAgo.with(ChronoField.DAY_OF_MONTH, oneMonthAgo.lengthOfMonth() / 2);

        var lastFullMoonUS = LocalDate.from(lastFullMoon);

        return todayInUS.getMonth() == lastFullMoonUS.getMonth();
    }

    private void sendAnnouncement(String moon, String label) {
        sendAnnouncement(moon + " \uD83D\uDC3A\n-# Hello to the " + label + " Moon!");
    }
}
