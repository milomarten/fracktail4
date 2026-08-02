package com.github.milomarten.fracktail.core.birthday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail.core.birthday.v2.BirthdayEventInstance;
import com.github.milomarten.fracktail.core.birthday.v2.UserBirthdayEventInstance;
import com.github.milomarten.fracktail.core.birthday.v3.MonthDayCalendar;
import com.github.milomarten.fracktail.core.persistence.Persistence;
import com.github.milomarten.fracktail.core.persistence.PersistenceBean;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BirthdayHandler implements PersistenceBean {
    private static final TypeReference<List<BirthdayCritter>> BIRTHDAY_TYPE = new TypeReference<List<BirthdayCritter>>() {};
    public static final String BIRTHDAYS_KEY = "birthdays";

    private final Persistence persistence;
    private final GatewayDiscordClient discordClient;
    private final BirthdayConfiguration birthdayConfiguration;

    Map<Snowflake, BirthdayEventInstance> birthdaysById;
    MonthDayCalendar<BirthdayEventInstance> birthdaysByDate;

    @PostConstruct
    private void initLoad() {
        load().block();
    }

    @Override
    public Mono<Void> load() {
        return persistence.retrieve(BIRTHDAYS_KEY, BIRTHDAY_TYPE)
                .switchIfEmpty(Mono.fromSupplier(List::of))
                .doOnSuccess(birthdays -> {
                    this.birthdaysById = new HashMap<>(birthdays.stream()
                            .map(bc -> bc.toEvent(discordClient))
                            .collect(Collectors.toMap(UserBirthdayEventInstance::userId, b -> b)));

                    this.birthdaysByDate = new MonthDayCalendar<>();
                    this.birthdaysById.forEach((id, bc) -> {
                        this.birthdaysByDate.addEvent(bc.getDayOfCelebration(), bc);
                    });

                    this.birthdayConfiguration.getHardCoded()
                            .forEach(bday -> this.birthdaysByDate.addEvent(bday.getDayOfCelebration(), bday));
                })
                .then();
    }

    @Override
    public Mono<Void> store() {
        var persistObj = this.birthdaysById.values()
                .stream()
                .filter(obj -> obj instanceof UserBirthdayEventInstance)
                .map(bei -> ((UserBirthdayEventInstance) bei).asCritter())
                .toList();
        return this.persistence.store(BIRTHDAYS_KEY, persistObj);
    }


    public Optional<BirthdayEventInstance> getBirthday(Snowflake user) {
        return Optional.ofNullable(this.birthdaysById.get(user));
    }

    public List<BirthdayEventInstance> getBirthdaysOn(LocalDate day) {
        return new ArrayList<>(birthdaysByDate.getItemsForDay(day));
    }

    public List<BirthdayEventInstance> getBirthdaysOn(MonthDay day) {
        return new ArrayList<>(birthdaysByDate.getItemsForDay(day));
    }

    public List<BirthdayEventInstance> getBirthdaysOn(Month month) {
        return new ArrayList<>(birthdaysByDate.getItemsForMonth(month));
    }

    public Optional<MonthDayCalendar.DayAndItem<List<BirthdayEventInstance>>> getNextBirthdays(LocalDate start) {
        return birthdaysByDate.getNextEvents(start);
    }

    public Optional<MonthDayCalendar.DayAndItem<List<BirthdayEventInstance>>> getPreviousBirthdays(LocalDate start) {
        return birthdaysByDate.getPreviousEvents(start);
    }

    public boolean hasBirthday(Snowflake critter) {
        return this.birthdaysById.containsKey(critter);
    }

    public Mono<Void> createBirthday(Snowflake critter, MonthDay day, Year year) {
        var newCritter = new BirthdayCritter(critter, day, year).toEvent(discordClient);

        this.birthdaysById.put(critter, newCritter);
        this.birthdaysByDate.addEvent(day, newCritter);
        return store();
    }
}
