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

    public Optional<BirthdayCalendar.NotNowBirthdayCritter> getNextBirthday(Snowflake user) {
        var now = LocalDate.now();
        return getBirthday(user)
                .map(bc -> {
                    var day = bc.getDay();
                    var deltaYear = day.isBefore(MonthDay.from(now)) ? 1 : 0;
                    return BirthdayCalendar.NotNowBirthdayCritter.from(bc, now.getYear() + deltaYear);
                });
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

    public Optional<BirthdayCalendar.NotNowBirthdayCritters> getNextBirthdays(LocalDate start) {
        return birthdaysByDate.getNextBirthday(start);
    }

    public Optional<BirthdayCalendar.NotNowBirthdayCritters> getPreviousBirthdays(LocalDate start) {
        return birthdaysByDate.getPreviousBirthday(start);
    }

    public List<BirthdayCritter> getBirthdays() {
        List<BirthdayCritter> birthdays = new ArrayList<>(birthdaysById.values());
        birthdays.sort(Comparator.comparing(BirthdayCritter::getDay));
        return birthdays;
    }

    public boolean hasBirthday(Snowflake critter) {
        return this.birthdaysById.containsKey(critter);
    }

    public int getNumberOfBirthdays() {
        return this.birthdaysById.size();
    }

    public Mono<Void> createBirthday(Snowflake critter, MonthDay day, Year year) {
        var newCritter = new BirthdayCritter(critter, day, year);

        if (this.birthdaysById.containsKey(critter)) {
            this.birthdaysById.put(critter, newCritter);
            this.birthdaysByDate.removeBirthday(newCritter);
            this.birthdaysByDate.addBirthday(newCritter);
        } else {
            this.birthdaysById.put(critter, newCritter);
            this.birthdaysByDate.addBirthday(newCritter);
        }
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
