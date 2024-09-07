package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.commands.BirthdaySlashCommand;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "discord.birthday.enabled", havingValue = "true")
public class BirthdayJob {
    private final BirthdayHandler handler;
    private final GatewayDiscordClient discordClient;

    @Value("${discord.birthday.announcementChannelId}")
    private Snowflake announcementChannelId;
    private TextChannel announcementChannel;

    public static final String HOME_TIMEZONE_RAW = "America/New_York";
    public static final ZoneId HOME_TIMEZONE = ZoneId.of(HOME_TIMEZONE_RAW);

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

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceBirthday() {
        var today = LocalDate.now(HOME_TIMEZONE);
        var birthdaysToday = handler.getBirthdaysOn(today);

        Flux.fromIterable(birthdaysToday)
                .filterWhen(bei -> bei.shouldDisplayForGuild(this.announcementChannel.getGuildId()))
                .flatMap(BirthdaySlashCommand::resolve)
                .collectList()
                .filter(Predicate.not(List::isEmpty))
                .map(birthdays -> {
                    return birthdays.stream()
                            .map(birthday -> {
                                var ageOptionally = birthday.getT1()
                                        .getStartYear()
                                        .map(year -> String.valueOf(today.getYear() - year.getValue()))
                                        .map(s -> "[" + s + "]")
                                        .orElse("");
                                return birthday.getT2() + " " + ageOptionally;
                            })
                            .collect(Collectors.joining(", ",
                                    "\uD83C\uDF89 It's Birthday Time! Happy Birthday to ",
                                    ""));
                })
                .flatMap(str -> announcementChannel.createMessage(str))
                .subscribe(null, ex -> log.error("Error sending birthday message", ex));
    }
}
