package com.github.milomarten.fracktail4.commands.birthday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail4.commands.birthday.v2.BirthdayEventInstance;
import com.github.milomarten.fracktail4.commands.birthday.v2.HardCodedBirthdayEventInstance;
import com.github.milomarten.fracktail4.commands.birthday.v2.UserBirthdayEventInstance;
import com.github.milomarten.fracktail4.persistence.Persistence;
import com.github.milomarten.fracktail4.persistence.PersistenceBean;
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

    Map<Snowflake, BirthdayEventInstance> birthdaysById;
    EventCalendar<BirthdayEventInstance> birthdaysByDate;

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

                    this.birthdaysByDate = new EventCalendar<>();
                    this.birthdaysById.forEach((id, bc) -> {
                        this.birthdaysByDate.addEvent(bc);
                    });

                    // Load static birthdays!
                    this.birthdaysByDate.addEvent(new HardCodedBirthdayEventInstance(
                            MonthDay.of(Month.APRIL, 2), null,
                            "Mom Marten",
                            Set.of(Snowflake.of(423976318082744321L))));

                    this.birthdaysByDate.addEvent(new HardCodedBirthdayEventInstance(
                            MonthDay.of(Month.MARCH, 6), null,
                            "Dad Marten",
                            Set.of(Snowflake.of(423976318082744321L))));
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
        return birthdaysByDate.getEventsOn(day);
    }

    public List<BirthdayEventInstance> getBirthdaysOn(MonthDay day) {
        return birthdaysByDate.getEventsOn(day);
    }

    public List<BirthdayEventInstance> getBirthdaysOn(Month month) {
        return birthdaysByDate.getEventsOn(month);
    }

    public Optional<EventCalendar.NotNowEvents<BirthdayEventInstance>> getNextBirthdays(LocalDate start) {
        return birthdaysByDate.getNextEvent(start);
    }

    public Optional<EventCalendar.NotNowEvents<BirthdayEventInstance>> getPreviousBirthdays(LocalDate start) {
        return birthdaysByDate.getPreviousEvent(start);
    }

    public List<BirthdayEventInstance> getBirthdays() {
        return birthdaysByDate.getEvents();
    }

    public boolean hasBirthday(Snowflake critter) {
        return this.birthdaysById.containsKey(critter);
    }

    public int getNumberOfBirthdays() {
        return this.birthdaysById.size();
    }

    public Mono<Void> createBirthday(Snowflake critter, MonthDay day, Year year) {
        var newCritter = new BirthdayCritter(critter, day, year).toEvent(discordClient);

        if (this.birthdaysById.containsKey(critter)) {
            this.birthdaysById.put(critter, newCritter);
            this.birthdaysByDate.removeEvent(newCritter);
            this.birthdaysByDate.addEvent(newCritter);
        } else {
            this.birthdaysById.put(critter, newCritter);
            this.birthdaysByDate.addEvent(newCritter);
        }
        return store();
    }
//
//    public Mono<Void> addYear(Snowflake critter, Year year) {
//        if (this.birthdaysById.containsKey(critter)) {
//            this.birthdaysById.get(critter).setYear(year);
//            return persist();
//        }
//        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
//    }
//
//    public Mono<Void> removeYear(Snowflake critter) {
//        if (this.birthdaysById.containsKey(critter)) {
//            this.birthdaysById.get(critter).setYear(null);
//            return persist();
//        }
//        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
//    }

    public Mono<Void> removeBirthday(Snowflake critter) {
        if (this.birthdaysById.containsKey(critter)) {
            var fullCritter = this.birthdaysById.remove(critter);
            this.birthdaysByDate.removeEvent(fullCritter);
            return store();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeBirthdays(List<BirthdayCritter> critters) {
        critters.forEach(s -> {
            var fullCritter = this.birthdaysById.remove(s.getCritter());
            this.birthdaysByDate.removeEvent(fullCritter);
        });
        return store();
    }
}
