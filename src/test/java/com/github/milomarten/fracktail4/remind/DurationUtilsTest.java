package com.github.milomarten.fracktail4.remind;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DurationUtilsTest {
    @Test
    public void testHoursOnly() {
        var duration = DurationUtils.stringToDuration("3hrs");
        assertEquals("PT3H", duration.toString());
    }

    @Test
    public void testMinutesOnly() {
        var duration = DurationUtils.stringToDuration("30mins");
        assertEquals("PT30M", duration.toString());
    }

    @Test
    public void testSecondsOnly() {
        var duration = DurationUtils.stringToDuration("30secs");
        assertEquals("PT30S", duration.toString());
    }

    @Test
    public void testHoursAndMinutes() {
        var duration = DurationUtils.stringToDuration("1hr30mins");
        assertEquals("PT1H30M", duration.toString());
    }

    @ParameterizedTest
    @CsvSource({
            "0.5hr", "1day", "12hems", "now"
    })
    public void testInvalidWords(String test) {
        assertThrows(IllegalStateException.class, () -> DurationUtils.stringToDuration(test));
    }

    @Test
    public void testSemiValidMinutes() {
        var duration = DurationUtils.stringToDuration("80m");
        assertEquals("PT1H20M", duration.toString());
    }

    @Test
    public void testDurationStringOneTerm() {
        var d = DurationUtils.durationToString(Duration.ofMinutes(10));
        assertEquals("10 minutes", d);
    }

    @Test
    public void testDurationStringTwoTerm() {
        var d = DurationUtils.durationToString(Duration.ofMinutes(10).plusHours(5));
        assertEquals("5 hours and 10 minutes", d);
    }

    @Test
    public void testDurationStringThreeTerm() {
        var d = DurationUtils.durationToString(Duration.ofMinutes(10).plusHours(36));
        assertEquals("1 day, 12 hours, and 10 minutes", d);
    }
}