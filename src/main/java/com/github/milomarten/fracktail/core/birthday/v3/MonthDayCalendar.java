package com.github.milomarten.fracktail.core.birthday.v3;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.*;

public class MonthDayCalendar<T> implements GenericCalendar<T> {
    private final NavigableMap<MonthDay, List<T>> items;

    public MonthDayCalendar() {
        this.items = new TreeMap<>();
    }

    public void addEvent(MonthDay when, T item) {
        this.items.computeIfAbsent(when, w -> new ArrayList<>()).add(item);
    }

    public void removeEvent(MonthDay when, T item) {
        if (this.items.containsKey(when)) {
            var items = this.items.get(when);
            items.remove(item);
            if (items.isEmpty()) {
                this.items.remove(when);
            }
        }
    }

    @Override
    public Collection<T> getItemsForDay(LocalDate when) {
        return new ArrayList<>(items.get(MonthDay.from(when)));
    }

    public Collection<T> getItemsForDay(MonthDay when) {
        return new ArrayList<>(items.get(when));
    }

    public Collection<T> getItemsForMonth(Month month) {
        SortedMap<MonthDay, List<T>> range =
                items.subMap(
                        MonthDay.of(month, 1),
                        true,
                        MonthDay.of(month, month.length(true)),
                        true
                );
        return range.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public record DayAndItem<T>(LocalDate when, T item) {}

    public Optional<DayAndItem<List<T>>> getNextEvents(LocalDate start) {
        if (items.isEmpty()) { return Optional.empty(); }

        var startCurrentYear = MonthDay.from(start);
        var year = start.getYear();

        var next = items.higherEntry(startCurrentYear);
        if (next == null) {
            year += 1;
            next = items.firstEntry();
        }

        return Optional.of(new DayAndItem<>(next.getKey().atYear(year), next.getValue()));
    }

    public Optional<DayAndItem<List<T>>> getPreviousEvents(LocalDate start) {
        if (items.isEmpty()) { return Optional.empty(); }

        var startCurrentYear = MonthDay.from(start);
        var year = start.getYear();

        var prev = items.lowerEntry(startCurrentYear);
        if (prev == null) {
            year -= 1;
            prev = items.lastEntry();
        }

        return Optional.of(new DayAndItem<>(prev.getKey().atYear(year), prev.getValue()));
    }
}
