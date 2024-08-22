package com.github.milomarten.fracktail4.birthday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
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
public class BirthdayHandler {
    private static final TypeReference<List<BirthdayCritter>> BIRTHDAY_TYPE = new TypeReference<List<BirthdayCritter>>() {};
    public static final String BIRTHDAYS_KEY = "birthdays";

    private final Persistence persistence;
    Map<Snowflake, BirthdayCritter> birthdaysById;
    BirthdayCalendar birthdaysByDate;

    @PostConstruct
    private void load() {
        var birthdays = persistence.retrieve(BIRTHDAYS_KEY, BIRTHDAY_TYPE).block();
        if (birthdays != null) {
            this.birthdaysById = new HashMap<>(birthdays.stream()
                    .collect(Collectors.toMap(BirthdayCritter::getCritter, b -> b)));
        } else {
            this.birthdaysById = new HashMap<>();
        }

        birthdaysByDate = new BirthdayCalendar();
        birthdaysById.forEach((id, bc) -> {
            birthdaysByDate.addBirthday(bc);
        });
    }

    private Mono<Void> persist() {
        return this.persistence.store(BIRTHDAYS_KEY, this.birthdaysById.values());
    }


    public Optional<BirthdayCritter> getBirthday(Snowflake user) {
        return Optional.ofNullable(this.birthdaysById.get(user));
    }

    public List<BirthdayCritter> getBirthdaysOn(LocalDate day) {
        return birthdaysByDate.getBirthdaysOn(day);
    }

    public List<BirthdayCritter> getBirthdaysOn(MonthDay day) {
        return birthdaysByDate.getBirthdaysOn(day);
    }

    public List<BirthdayCritter> getBirthdaysOn(Month month) {
        return birthdaysByDate.getBirthdaysOn(month);
    }

    public Optional<BirthdayCalendar.FutureBirthdayCritters> getNextBirthdays(LocalDate start) {
        return birthdaysByDate.getNextBirthday(start);
    }

    public Optional<BirthdayCalendar.PastBirthdayCritters> getPreviousBirthdays(LocalDate start) {
        return birthdaysByDate.getPreviousBirthday(start);
    }

    public boolean hasBirthday(Snowflake critter) {
        return this.birthdaysById.containsKey(critter);
    }

    public Mono<Void> createBirthday(Snowflake critter, MonthDay day, Year year) {
        if (this.birthdaysById.containsKey(critter)) {
            return Mono.error(new IllegalArgumentException("Critter already has a birthday!"));
        }

        var newCritter = new BirthdayCritter(critter, day, year);
        this.birthdaysById.put(critter, newCritter);
        this.birthdaysByDate.addBirthday(newCritter);
        return persist();
    }

    public Mono<Void> addYear(Snowflake critter, Year year) {
        if (this.birthdaysById.containsKey(critter)) {
            this.birthdaysById.get(critter).setYear(year);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeYear(Snowflake critter) {
        if (this.birthdaysById.containsKey(critter)) {
            this.birthdaysById.get(critter).setYear(null);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeBirthday(Snowflake critter) {
        if (this.birthdaysById.containsKey(critter)) {
            var fullCritter = this.birthdaysById.remove(critter);
            this.birthdaysByDate.removeBirthday(fullCritter);
            return persist();
        }
        return Mono.error(new IllegalArgumentException("Critter does not have a birthday"));
    }

    public Mono<Void> removeBirthdays(List<BirthdayCritter> critters) {
        critters.forEach(s -> {
            var fullCritter = this.birthdaysById.remove(s.getCritter());
            this.birthdaysByDate.removeBirthday(fullCritter);
        });
        return persist();
    }
}