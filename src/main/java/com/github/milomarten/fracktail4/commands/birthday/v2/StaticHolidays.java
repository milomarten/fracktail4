package com.github.milomarten.fracktail4.commands.birthday.v2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.MonthDay;

/**
 * List of holidays with a constant day of the year.
 * These interface well with the EventCalendar object, which can only function on static days.
 * <br>
 * At initialization, all of these Holidays are compiled into an EventCalendar
 * which contains them.
 */
@RequiredArgsConstructor
@Getter
public enum StaticHolidays implements EventInstance {
    WOLFENOOT("Wolfenoot", "Happy", MonthDay.of(11, 23)),
    CHRISTMAS("Christmas", "Merry", MonthDay.of(12, 25)),
    HALLOWEEN("Halloween", "Happy", MonthDay.of(10, 31)),
    NEW_YEARS("New Years", "Happy", MonthDay.of(1, 1)),
    JUNETEENTH("Juneteenth", "Happy", MonthDay.of(6, 19)),
    MAY_DAY("May Day", "Happy", MonthDay.of(5, 1)),
    GROUNDHOG_DAY("Groundhog Day", "Happy", MonthDay.of(2, 2))
    ;

    private final String name;
    private final String greeting;
    private final MonthDay dayOfCelebration;
}
