package com.github.milomarten.fracktail.core.birthday.v2;

import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.*;
import java.util.stream.IntStream;

public class EventCalendar<T extends EventInstance> {
    private final List[] holder;
    private int size = 0;

    public EventCalendar() {
        // 1 = Jan 1, 366 = Dec 31st. 0 is sentinel for rollover.
        this.holder = new List[367]; // Sure hope they don't add a new day to the calendar
    }

    private int getIndexForMonthDay(Month month, int day) {
        return month.firstDayOfYear(true) + day - 1;
    }

    private int getIndexForMonthDay(MonthDay monthDay) {
        return getIndexForMonthDay(monthDay.getMonth(), monthDay.getDayOfMonth());
    }

    private int getIndexForLocalDate(LocalDate localDate) {
        // We can't use localDate.getDayOfYear, because that doesn't keep track of leap years correctly.
        return getIndexForMonthDay(localDate.getMonth(), localDate.getDayOfMonth());
    }

    public void addEvent(T critter) {
        var idx = getIndexForMonthDay(critter.getDayOfCelebration());
        var ctr = this.holder[idx];
        if (ctr == null) {
            var newArray = new ArrayList<>();
            newArray.add(critter);
            this.holder[idx] = newArray;
        } else {
            ctr.add(critter);
        }
        size++;
    }

    public boolean removeEvent(T critter) {
        var idx = getIndexForMonthDay(critter.getDayOfCelebration());
        var ctr = this.holder[idx];
        if (ctr == null) {
            return false;
        } else {
            int currentCtrSize = ctr.size();
            var found = ctr.remove(critter);
            if (found) {
                this.size -= (currentCtrSize - ctr.size());
            }
            return found;
        }
    }

    public List<T> getEventsOn(LocalDate origin) {
        var idx = getIndexForLocalDate(origin);
        var ctr = this.holder[idx];
        if (CollectionUtils.isEmpty(ctr)) {
            return List.of();
        } else {
            return new ArrayList<>(ctr);
        }
    }

    public List<T> getEventsOn(MonthDay origin) {
        var idx = getIndexForMonthDay(origin);
        var ctr = this.holder[idx];
        if (CollectionUtils.isEmpty(ctr)) {
            return List.of();
        } else {
            return new ArrayList<>(ctr);
        }
    }

    public List<T> getEventsOn(Month month) {
        int lowerEnd = getIndexForMonthDay(month, 1);
        int upperEnd = getIndexForMonthDay(month, month.length(true));

        return IntStream.rangeClosed(lowerEnd, upperEnd)
                .mapToObj(idx -> this.holder[idx])
                .filter(Objects::nonNull)
                .flatMap(l -> l.stream())
                .toList();
    }

    public Optional<NotNowEvents<T>> getNextEvent(LocalDate origin) {
        if (this.size == 0) {
            // No amount of looping will help...
            return Optional.empty();
        }

        var year = origin.getYear();
        for (var idx = getIndexForLocalDate(origin) + 1 ;; idx = (idx + 1) % this.holder.length) {
            if (idx == 0) {
                year++;
            } else {
                var ctr = this.holder[idx];
                if (CollectionUtils.isNotEmpty(ctr)) {
                    return Optional.of(NotNowEvents.from(ctr, year));
                }
            }
        }
    }

    public Optional<NotNowEvents<T>> getPreviousEvent(LocalDate origin) {
        if (this.size == 0) {
            // No amount of looping will help...
            return Optional.empty();
        }

        var year = origin.getYear();
        for (var idx = getIndexForLocalDate(origin) - 1 ;; idx = rollingDecrement(idx)) {
            if (idx == 0) {
                year++;
            } else {
                var ctr = this.holder[idx];
                if (CollectionUtils.isNotEmpty(ctr)) {
                    return Optional.of(NotNowEvents.from(ctr, year));
                }
            }
        }
    }

    private int rollingDecrement(int i) {
        i -= 1;
        if (i < 0) { i += this.holder.length; }
        return i;
    }

    public List<BirthdayEventInstance> getEvents() {
        return Arrays.stream(this.holder)
                .filter(Objects::nonNull)
                .flatMap(l -> l.stream())
                .toList();
    }

    public record NotNowEvents<T extends EventInstance> (List<T> celebrators, LocalDate when) {
        public static <T extends EventInstance> NotNowEvents<T> from(List<T> critters, int year) {
            var when = critters.get(0).getDayOfCelebration().atYear(year);
            var whos = new ArrayList<>(critters);
            return new NotNowEvents<>(whos, when);
        }
    }
}
