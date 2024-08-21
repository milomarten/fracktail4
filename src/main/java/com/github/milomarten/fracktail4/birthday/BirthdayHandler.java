package com.github.milomarten.fracktail4.birthday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IteratorUtils;
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
public class BirthdayHandler {
    private static final TypeReference<List<BirthdayCritter>> BIRTHDAY_TYPE = new TypeReference<List<BirthdayCritter>>() {};
    public static final Comparator<BirthdayCritter> BIRTHDAY_COMPARATOR = Comparator.comparing(BirthdayCritter::getDay);
    public static final String BIRTHDAYS_KEY = "birthdays";

    private final Persistence persistence;
    Map<Snowflake, BirthdayCritter> birthdays;

    @PostConstruct
    private void load() {
        var birthdays = persistence.retrieve(BIRTHDAYS_KEY, BIRTHDAY_TYPE).block();
        if (birthdays != null) {
            this.birthdays = new HashMap<>(birthdays.stream()
                    .collect(Collectors.toMap(BirthdayCritter::getCritter, b -> b)));
        } else {
            this.birthdays = new HashMap<>();
        }
    }

    public Optional<BirthdayCritter> getBirthday(Snowflake user) {
        return Optional.ofNullable(this.birthdays.get(user));
    }

    public List<BirthdayCritter> getBirthdaysOn(MonthDay day) {
        return birthdays.values()
                .stream()
                .filter(bc -> bc.getDay().equals(day))
                .toList();
    }

    public List<BirthdayCritter> getBirthdaysOn(Month month) {
        return birthdays.values()
                .stream()
                .filter(bc -> bc.getDay().getMonth().equals(month))
                .toList();
    }
//
//    public List<BirthdayCritter> getNextBirthdays(LocalDate origin) {
//        return getNextBirthdayDate(origin)
//                .map(ld -> getBirthdaysOn(MonthDay.from(origin)))
//                .orElseGet(List::of);
//    }

    public Optional<LocalDate> getNextBirthdayDate(LocalDate start) {
        if (birthdays.isEmpty()) {
            return Optional.empty();
        }

        var sortedBirthdays = birthdays.values().stream()
                .sorted(BIRTHDAY_COMPARATOR)
                .toList();

        var startMonthDay = MonthDay.from(start);
        for (BirthdayCritter toCheck : sortedBirthdays) {
            if (toCheck.getDay().isAfter(startMonthDay)) {
                return Optional.of(toCheck.getDay().atYear(start.getYear()));
            }
        }
        // Rolled off the year, so the first one of the next year is the next birthday!
        return Optional.of(sortedBirthdays.get(0).getDay().atYear(start.getYear() + 1));
    }

    private boolean isAfter(LocalDate one, MonthDay two) {
        return MonthDay.from(one).isAfter(two);
    }

    public boolean hasBirthday(Snowflake critter) {
        return this.birthdays.containsKey(critter);
    }

    private Mono<Void> persist() {
        return this.persistence.store(BIRTHDAYS_KEY, this.birthdays.values());
    }

    public Mono<Void> createBirthday(Snowflake critter, MonthDay day, Year year) {
        if (this.birthdays.containsKey(critter)) {
            return Mono.error(new IllegalArgumentException("Critter already has a birthday!"));
        }

        this.birthdays.put(critter, new BirthdayCritter(critter, day, year));
        return persist();
    }

    public Mono<Void> addYear(Snowflake critter, Year year) {
        if (this.birthdays.containsKey(critter)) {
            this.birthdays.get(critter).setYear(year);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeYear(Snowflake critter) {
        if (this.birthdays.containsKey(critter)) {
            this.birthdays.get(critter).setYear(null);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeBirthday(Snowflake critter) {
        if (this.birthdays.containsKey(critter)) {
            this.birthdays.remove(critter);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeBirthdays(List<BirthdayCritter> critters) {
        critters.forEach(s -> this.birthdays.remove(s.getCritter()));
        return persist();
    }
}
