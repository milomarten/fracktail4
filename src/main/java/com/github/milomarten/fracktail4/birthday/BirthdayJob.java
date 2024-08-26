package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.commands.BirthdayInstance;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(GatewayDiscordClient.class)
public class BirthdayJob {
    private final BirthdayHandler handler;
    private final GatewayDiscordClient discordClient;

    @Value("${birthday.announcement.channel}")
    private Snowflake announcementChannelId;
    private TextChannel announcementChannel;

    @Value("${birthday.announcement.timezone}")
    private ZoneId homeTimezone;

    @PostConstruct
    public void setUp() {
        this.announcementChannel = discordClient.getChannelById(announcementChannelId)
                .cast(TextChannel.class)
                .blockOptional()
                .orElseThrow();
        log.info("Birthday announcement channel: {}", this.announcementChannel.getMention());
    }

    @Scheduled(cron = "@midnight", zone = "America/New_York")
    public void announceBirthday() {
        var today = LocalDate.now(homeTimezone);
        var thisYear = Year.from(today);
        var birthdaysToday = handler.getBirthdaysOn(today);

        Flux.fromIterable(birthdaysToday)
                .flatMap(birthday -> {
                    return discordClient.getMemberById(announcementChannel.getGuildId(), birthday.getCritter())
                            .onErrorResume(ex -> {
                                log.error("Error getting member. Will not announce them.", ex);
                                return Mono.empty();
                            })
                            .map(member -> new BirthdayInstance(birthday, member));
                })
                .collectList()
                .filter(Predicate.not(List::isEmpty))
                .map(birthdays -> {
                    return birthdays.stream()
                            .map(birthday -> {
                                var ageOptionally = birthday.getAge(thisYear)
                                        .map(s -> "[" + s + "]")
                                        .orElse("");
                                return birthday.getName() + " " + ageOptionally;
                            })
                            .collect(Collectors.joining(", ",
                                    "\uD83C\uDF89 It's Birthday Time! Happy Birthday to ",
                                    ""));
                })
                .flatMap(str -> announcementChannel.createMessage(str))
                .subscribe(null, ex -> log.error("Error sending birthday message", ex));
    }
}
