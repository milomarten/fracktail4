package com.github.milomarten.fracktail4.birthday;

import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.MonthDay;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BirthdayHandlerTest {
    @Mock private Persistence persistence;
    @InjectMocks private BirthdayHandler birthdayHandler;

    @Test
    public void testNextBirthday_Single() {
        Map<Snowflake, BirthdayCritter> birthdayMap = Map.of(
                Snowflake.of(1), new BirthdayCritter(Snowflake.of(1), MonthDay.of(1, 1)),
                Snowflake.of(2), new BirthdayCritter(Snowflake.of(2), MonthDay.of(10, 1)),
                Snowflake.of(3), new BirthdayCritter(Snowflake.of(3), MonthDay.of(12, 1))
        );

        birthdayHandler.birthdays = birthdayMap;
        var nextBirthdays = birthdayHandler.getNextBirthdays(MonthDay.of(7, 1));

        assertEquals(1, nextBirthdays.size());
        assertEquals(2, nextBirthdays.get(0).getCritter().asLong());
    }

    @Test
    public void testNextBirthday_Multi() {
        Map<Snowflake, BirthdayCritter> birthdayMap = Map.of(
                Snowflake.of(1), new BirthdayCritter(Snowflake.of(1), MonthDay.of(12, 1)),
                Snowflake.of(2), new BirthdayCritter(Snowflake.of(2), MonthDay.of(12, 1))
        );

        birthdayHandler.birthdays = birthdayMap;
        var nextBirthdays = birthdayHandler.getNextBirthdays(MonthDay.of(7, 1));

        assertEquals(2, nextBirthdays.size());
        var snowflakes = nextBirthdays.stream()
                .map(BirthdayCritter::getCritter)
                .map(Snowflake::asLong)
                .map(Long::intValue)
                .collect(Collectors.toSet());
        assertTrue(snowflakes.containsAll(Set.of(1, 2)));
    }

    @Test
    public void testNextBirthday_SingleRollover() {
        Map<Snowflake, BirthdayCritter> birthdayMap = Map.of(
                Snowflake.of(1), new BirthdayCritter(Snowflake.of(1), MonthDay.of(1, 1)),
                Snowflake.of(2), new BirthdayCritter(Snowflake.of(2), MonthDay.of(10, 1)),
                Snowflake.of(3), new BirthdayCritter(Snowflake.of(3), MonthDay.of(12, 1))
        );

        birthdayHandler.birthdays = birthdayMap;
        var nextBirthdays = birthdayHandler.getNextBirthdays(MonthDay.of(12, 16));

        assertEquals(1, nextBirthdays.size());
        assertEquals(1, nextBirthdays.get(0).getCritter().asLong());
    }

    @Test
    public void testNextBirthday_MultiRollover() {
        Map<Snowflake, BirthdayCritter> birthdayMap = Map.of(
                Snowflake.of(1), new BirthdayCritter(Snowflake.of(1), MonthDay.of(1, 1)),
                Snowflake.of(4), new BirthdayCritter(Snowflake.of(4), MonthDay.of(1, 1)),
                Snowflake.of(2), new BirthdayCritter(Snowflake.of(2), MonthDay.of(10, 1)),
                Snowflake.of(3), new BirthdayCritter(Snowflake.of(3), MonthDay.of(12, 1))
        );

        birthdayHandler.birthdays = birthdayMap;
        var nextBirthdays = birthdayHandler.getNextBirthdays(MonthDay.of(12, 16));

        assertEquals(2, nextBirthdays.size());
        var snowflakes = nextBirthdays.stream()
                .map(BirthdayCritter::getCritter)
                .map(Snowflake::asLong)
                .map(Long::intValue)
                .collect(Collectors.toSet());
        assertTrue(snowflakes.containsAll(Set.of(1, 4)));
    }

    @Test
    public void testNextBirthday_SameDay() {
        Map<Snowflake, BirthdayCritter> birthdayMap = Map.of(
                Snowflake.of(1), new BirthdayCritter(Snowflake.of(1), MonthDay.of(1, 1)),
                Snowflake.of(2), new BirthdayCritter(Snowflake.of(2), MonthDay.of(10, 1)),
                Snowflake.of(3), new BirthdayCritter(Snowflake.of(3), MonthDay.of(12, 1))
        );

        birthdayHandler.birthdays = birthdayMap;
        var nextBirthdays = birthdayHandler.getNextBirthdays(MonthDay.of(10, 1));

        assertEquals(1, nextBirthdays.size());
        assertEquals(3, nextBirthdays.get(0).getCritter().asLong());
    }
}