package com.github.milomarten.fracktail.core.job;

import com.github.milomarten.fracktail.core.birthday.v2.BirthdayHandler;
import com.github.milomarten.fracktail.core.birthday.v2.EventCalendar;
import com.github.milomarten.fracktail.core.birthday.v2.BirthdayEventInstance;
import com.github.milomarten.fracktail.core.birthday.v2.DynamicHolidays;
import com.github.milomarten.fracktail.core.birthday.v2.StaticHolidays;
import discord4j.common.util.Snowflake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "discord.birthday.enabled", havingValue = "true")
public class BirthdayJob extends AbstractAnnouncementJob {
    private final BirthdayHandler handler;
    private final EventCalendar<StaticHolidays> holidayCalendar;

    BirthdayJob(BirthdayHandler handler, EventCalendar<StaticHolidays> holidayCalendar,
            @Value("${discord.birthday.announcementChannelId}") Snowflake announcementChannelId) {
        super(announcementChannelId);
        this.handler = handler;
        this.holidayCalendar = holidayCalendar;
    }

    public static final String HOME_TIMEZONE_RAW = "America/New_York";
    public static final ZoneId HOME_TIMEZONE = ZoneId.of(HOME_TIMEZONE_RAW);

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceBirthday() {
        var today = LocalDate.now(HOME_TIMEZONE);
        var birthdaysToday = handler.getBirthdaysOn(today);

        Flux.fromIterable(birthdaysToday)
                .filterWhen(bei -> bei.shouldDisplayForGuild(getAnnouncementChannel().getGuildId()))
                .flatMap(BirthdayEventInstance::resolve)
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
                                    "<@&1366975961932894278> \uD83C\uDF89 It's Birthday Time! Happy Birthday to ",
                                    ""));
                })
                .doOnSuccess(this::sendAnnouncement)
                .subscribe(null, ex -> log.error("Error sending birthday message", ex));
    }

    public void checkBirthdayAndAnnounceIfNecessary(Snowflake userId) {
        var today = LocalDate.now(HOME_TIMEZONE);
        var specificBirthdayOpt = handler.getBirthday(userId);

        if (specificBirthdayOpt.isEmpty()) { return; }
        var specificBirthday = specificBirthdayOpt.get();

        if (MonthDay.from(today).equals(specificBirthday.getDayOfCelebration())) {
            specificBirthday.resolve()
                    .filterWhen(bei -> bei.getT1().shouldDisplayForGuild(getAnnouncementChannel().getGuildId()))
                    .map(birthday -> {
                        var ageOptionally = birthday.getT1()
                                .getStartYear()
                                .map(year -> String.valueOf(today.getYear() - year.getValue()))
                                .map(s -> "[" + s + "]")
                                .orElse("");
                        return birthday.getT2() + " " + ageOptionally;
                    })
                    .map(text -> String.format("<@&1366975961932894278> \uD83C\uDF89 It's Birthday Time! Happy Birthday to %s", text))
                    .doOnSuccess(this::sendAnnouncement)
                    .subscribe(null, ex -> log.error("Error sending birthday message", ex));
        }
    }

    @Scheduled(cron = "@midnight", zone = HOME_TIMEZONE_RAW)
    public void announceHoliday() {
        var today = LocalDate.now(HOME_TIMEZONE);

        var normalHolidays = Flux.fromIterable(holidayCalendar.getEventsOn(today))
                .map(h -> Tuples.of(h.getGreeting(), h.getName()));
        var dynamicHolidays = Flux.fromIterable(DynamicHolidays.getEventsOn(today))
                .map(h -> Tuples.of(h.getGreeting(), h.getName()));

        Flux.concat(normalHolidays, dynamicHolidays)
                .collectList()
                .doOnSuccess(h -> {
                    h.forEach(tuple -> {
                        String message = "%s %s, everyone!".formatted(tuple.getT1(), tuple.getT2());
                        this.sendAnnouncement(message);
                    });
                })
                .subscribe(null, ex -> log.error("Error sending holiday message", ex));
    }
}
