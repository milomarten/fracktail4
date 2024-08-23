package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.commands.BirthdayInstance;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class BirthdayJob {
    private final BirthdayHandler handler;
    private final GatewayDiscordClient discordClient;

    private static final Snowflake HOME_SERVER = Snowflake.of(423976318082744321L);
    private static final Snowflake ANNOUNCEMENT_CHANNEL = Snowflake.of(746898862098087977L);

    private static final String HOME_TIMEZONE_RAW = "America/New_York";
    private static final ZoneId HOME_TIMEZONE = ZoneId.of(HOME_TIMEZONE_RAW);

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceBirthday() {
        var today = LocalDate.now(HOME_TIMEZONE);
        var birthdaysToday = handler.getBirthdaysOn(today);

        Flux.fromIterable(birthdaysToday)
                .flatMap(birthday -> {
                    return discordClient.getMemberById(HOME_SERVER, birthday.getCritter())
                            .onErrorResume(ex -> {
                                log.error("Error getting member", ex);
                                return Mono.empty();
                            })
                            .map(member -> new BirthdayInstance(birthday, member));
                })
                .collectList()
                .filter(Predicate.not(List::isEmpty))
                .map(birthdays -> {
                    return birthdays.stream()
                            .map(birthday -> {
                                var ageOptionally = birthday.celebrator()
                                        .getYear()
                                        .map(year -> String.valueOf(today.getYear() - year.getValue()))
                                        .map(s -> "[" + s + "]")
                                        .orElse("");
                                return birthday.getName() + " " + ageOptionally;
                            })
                            .collect(Collectors.joining(", ",
                                    "\uD83C\uDF89 It's Birthday Time! Happy Birthday to ",
                                    ""));
                })
                .flatMap(str -> discordClient.getChannelById(ANNOUNCEMENT_CHANNEL)
                        .cast(TextChannel.class)
                        .flatMap(c -> c.createMessage(str))
                )
                .subscribe(null, ex -> log.error("Error sending birthday message", ex));
    }
}
