package com.github.milomarten.fracktail.core.birthday.v3;

import lombok.Getter;

import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.util.*;

/**
 * A simple calendar for a given year.
 * @param <T> The type of event in the calendar.
 */
public class CalendarForYear<T> {
    private final TreeMap<MonthDay, List<T>> holder;
    @Getter private final Year year;

    /**
     * Create a blank calendar for this year.
     * @param year The year to make a calendar for.
     */
    public CalendarForYear(Year year) {
        this.year = year;
        this.holder = new TreeMap<>(Comparator.comparing(this::getIndexForMonthDay));
    }

    private int getIndexForMonthDay(MonthDay md) {
        return md.getMonth().firstDayOfYear(year.isLeap()) + md.getDayOfMonth() - 1;
    }

    private MonthDay normalizeForLeapYear(MonthDay md) {
        if (md.isValidYear(year.getValue())) {
            return md;
        } else {
            return MonthDay.from(md.atYear(year.getValue()));
        }
    }

    /**
     * Add an event to the calendar
     * Attempting to add an event on February 29th for a non-leap year will instead
     * add that event on February 28th.
     * @param day The day of the event
     * @param event The event itself
     */
    public void addEvent(MonthDay day, T event) {
        day = normalizeForLeapYear(day);
        holder.computeIfAbsent(day, d -> new ArrayList<>()).add(event);
    }

    /**
     * Remove an event from the calendar
     * Attempting to remove an event on February 29th for a non-leap year will
     * instead remove that event from February 28th.
     * @param day The day of the event
     * @param event The event itself
     */
    public void removeEvent(MonthDay day, T event) {
        day = normalizeForLeapYear(day);
        if (holder.containsKey(day)) {
            var list = holder.get(day);
            list.remove(event);
            if (list.isEmpty()) {
                holder.remove(day);
            }
        }
    }

    /**
     * Retrieve all events on this day
     * Attempting to retrieve events on February 29th on a non-leap year will
     * instead retrieve events for February 28th
     * @param when When to get events
     * @return A list of events, empty if none.
     */
    public List<T> getEventsOn(MonthDay when) {
        when = normalizeForLeapYear(when);
        return holder.getOrDefault(when, List.of());
    }

    /**
     * Retrieve events in a given month
     * @param month The month to retrieve
     * @return A list of events and when they occur.
     */
    public List<EventAndWhen<T>> getEventsOn(Month month) {
        var range = holder.subMap(
                MonthDay.of(month, 1), true,
                MonthDay.of(month, month.length(year.isLeap())), true
        );

        var returnList = new ArrayList<EventAndWhen<T>>();
        range.forEach((when, events) -> {
            var hydratedWhen = when.atYear(year.getValue());
            events.stream()
                    .map(evt -> new EventAndWhen<>(evt, hydratedWhen))
                    .forEach(returnList::add);
        });

        return returnList;
    }

    /**
     * Get the events on the first populated calendar day of the year
     * @return A list of events and when they occur, or null if the calendar is empty
     */
    public EventAndWhen<List<T>> getFirstEvents() {
        var first = holder.firstEntry();
        if (first == null) {
            return null;
        }
        var hydratedWhen = first.getKey().atYear(year.getValue());
        return new EventAndWhen<>(new ArrayList<>(first.getValue()), hydratedWhen);
    }

    /**
     * Get the events on the most upcoming calendar day relative to some origin day
     * @param origin The date to start looking
     * @param exclusive If true, origin is excluded from the results.
     * @return A list of events and when they occur, or null if the calendar is empty
     */
    public EventAndWhen<List<T>> getNextEvents(MonthDay origin, boolean exclusive) {
        var next =
                exclusive ? holder.higherEntry(origin) : holder.ceilingEntry(origin);
        if (next == null) {
            return null;
        }
        var hydratedWhen = next.getKey().atYear(year.getValue());
        return new EventAndWhen<>(new ArrayList<>(next.getValue()), hydratedWhen);
    }

    /**
     * Get the events on the most previous calendar day relative to some origin day
     * @param origin The date to start looking
     * @param exclusive If true, origin is excluded from the results.
     * @return A list of events and when they occur, or null if the calendar is empty
     */
    public EventAndWhen<List<T>> getPreviousEvents(MonthDay origin, boolean exclusive) {
        var previous =
                exclusive ? holder.lowerEntry(origin) : holder.floorEntry(origin);
        if (previous == null) {
            return null;
        }
        var hydratedWhen = previous.getKey().atYear(year.getValue());
        return new EventAndWhen<>(new ArrayList<>(previous.getValue()), hydratedWhen);
    }

    /**
     * Get the events on the last populated calendar day of the year
     * @return A list of events and when they occur, or null if the calendar is empty
     */
    public EventAndWhen<List<T>> getLastEvents() {
        var last = holder.lastEntry();
        if (last == null) {
            return null;
        }
        var hydratedWhen = last.getKey().atYear(year.getValue());
        return new EventAndWhen<>(new ArrayList<>(last.getValue()), hydratedWhen);
    }
}
