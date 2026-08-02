package com.github.milomarten.fracktail.core.birthday.v3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

/**
 * List of holidays that can vary on when their day is.
 * All of these holidays so far are in the form of "The [nth|first|last] [day of the week] in [month]",
 * but any type of holiday could be supported here, such as Easter.
 * <br>
 * Every day at midnight, these holidays are queried to determine if they are happening. As such,
 * it's important to `return` as fast as possible, to avoid unnecessary computation. For the above
 * format, it's best practice to:
 * 1. Check the month (most limiting check, requires only a retrieval and comparison)
 * 2. Check the range of valid days of the month (next most limiting, one retrieval two comparisons),
 * 3. Check the day of the week (Complex math to compute)
 */
@RequiredArgsConstructor
@Getter
public enum DynamicHolidays implements Holiday {
    ARBOR_DAY("Arbor Day", "Happy") {
        @Override
        public boolean isOnDay(LocalDate when) {
            // Arbor Day is the last Friday of April.
            return when.getMonth() == Month.APRIL &&
                    Month.APRIL.length(when.isLeapYear()) - when.getDayOfMonth() < 7 &&
                    when.getDayOfWeek() == DayOfWeek.FRIDAY;
        }
    },
    LABOR_DAY("Labor Day", "Todayborday is") {
        @Override
        public boolean isOnDay(LocalDate when) {
            // Labor Day is the first Monday of September.
            return when.getMonth() == Month.SEPTEMBER &&
                    when.getDayOfMonth() <= 7 &&
                    when.getDayOfWeek() == DayOfWeek.MONDAY;
        }
    },
    AMERICAN_THANKSGIVING("(American) Thanksgiving", "Happy") {
        @Override
        public boolean isOnDay(LocalDate when) {
            //American Thanksgiving is the 4th Thursday of November.
            return isOnNthDayOfTheMonth(when, 4, DayOfWeek.THURSDAY, Month.NOVEMBER);
        }
    },
    CANADIAN_THANKSGIVING("(Canadian) Thanksgiving", "Happy") {
        @Override
        public boolean isOnDay(LocalDate when) {
            //Canadian Thanksgiving is the 2nd Monday in October.
            return isOnNthDayOfTheMonth(when, 2, DayOfWeek.MONDAY, Month.OCTOBER);
        }
    };

    private final String name;
    private final String greeting;

    public abstract boolean isOnDay(LocalDate when);

    /**
     * Check if the day is the nth ofTheWeek in month
     * @param test The date to test
     * @param n The week of the month number, 1 through 5
     * @param ofTheWeek The day of the week to test for
     * @param month The month of the year to test for
     * @return True, if the test date matches all parameters.
     */
    protected static boolean isOnNthDayOfTheMonth(LocalDate test, int n, DayOfWeek ofTheWeek, Month month) {
        return test.getMonth() == month &&
                test.getDayOfMonth() >= ((7 * (n - 1)) + 1) &&
                test.getDayOfMonth() <= (7 * n) &&
                test.getDayOfWeek() == ofTheWeek;
    }
}
