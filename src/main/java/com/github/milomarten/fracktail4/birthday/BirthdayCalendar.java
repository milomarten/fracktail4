package com.github.milomarten.fracktail4.birthday;

import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class BirthdayCalendar {
    private final List[] holder;
    private int size = 0;

    public BirthdayCalendar() {
        // 1 = Jan 1, 366 = Dec 31st. 0 is sentinel for rollover.
        this.holder = new List[367]; // Sure hope they don't add a new day to the calendar
    }

    private int getIndexForMonthDay(Month month, int day) {
        return month.firstDayOfYear(true) + day;
    }

    private int getIndexForMonthDay(MonthDay monthDay) {
        return getIndexForMonthDay(monthDay.getMonth(), monthDay.getDayOfMonth());
    }

    private int getIndexForLocalDate(LocalDate localDate) {
        // We can't use localDate.getDayOfYear, because that doesn't keep track of leap years correctly.
        return getIndexForMonthDay(localDate.getMonth(), localDate.getDayOfMonth());
    }

    private MonthDay getMonthDayForIndex(int idx) {
        Month moy = Month.of((idx - 1) / 31 + 1);
        int monthEnd = moy.firstDayOfYear(true) + moy.length(true) - 1;
        if (idx > monthEnd) {
            moy = moy.plus(1);
        }
        int dom = idx - moy.firstDayOfYear(true) + 1;
        return MonthDay.of(moy, dom);
    }

    public void addBirthday(BirthdayCritter critter) {
        var idx = getIndexForMonthDay(critter.getDay());
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

    public boolean removeBirthday(BirthdayCritter critter) {
        var idx = getIndexForMonthDay(critter.getDay());
        var ctr = this.holder[idx];
        if (ctr == null) {
            return false;
        } else {
            int currentCtrSize = ctr.size();
            var found = ctr.removeIf(i -> ((BirthdayCritter)i).getCritter().equals(critter.getCritter()));
            if (found) {
                this.size -= (currentCtrSize - ctr.size());
            }
            return found;
        }
    }

    public List<BirthdayCritter> getBirthdaysOn(LocalDate origin) {
        var idx = getIndexForLocalDate(origin);
        var ctr = this.holder[idx];
        if (CollectionUtils.isEmpty(ctr)) {
            return List.of();
        } else {
            return new ArrayList<>(ctr);
        }
    }

    public List<BirthdayCritter> getBirthdaysOn(MonthDay origin) {
        var idx = getIndexForMonthDay(origin);
        var ctr = this.holder[idx];
        if (CollectionUtils.isEmpty(ctr)) {
            return List.of();
        } else {
            return new ArrayList<>(ctr);
        }
    }

    public List<BirthdayCritter> getBirthdaysOn(Month month) {
        int lowerEnd = getIndexForMonthDay(month, 1);
        int upperEnd = getIndexForMonthDay(month, month.length(true));

        return IntStream.rangeClosed(lowerEnd, upperEnd)
                .mapToObj(idx -> this.holder[idx])
                .filter(Objects::nonNull)
                .flatMap(l -> l.stream())
                .toList();
    }

    public Optional<FutureBirthdayCritters> getNextBirthday(LocalDate origin) {
        if (this.size == 0) {
            // No amount of looping will help...
            return Optional.empty();
        }

        var rollover = false;
        for (var idx = getIndexForLocalDate(origin) + 1 ;; idx = (idx + 1) % this.holder.length) {
            if (idx == 0) {
                if (rollover) { return Optional.empty(); } // Safety, should never trigger??
                rollover = true;
            } else {
                var ctr = this.holder[idx];
                if (CollectionUtils.isNotEmpty(ctr)) {
                    return Optional.of(new FutureBirthdayCritters(ctr, rollover));
                }
            }
        }
    }

    public Optional<PastBirthdayCritters> getPreviousBirthday(LocalDate origin) {
        if (this.size == 0) {
            // No amount of looping will help...
            return Optional.empty();
        }

        var rollback = false;
        for (var idx = getIndexForLocalDate(origin) - 1 ;; idx = rollingDecrement(idx)) {
            if (idx == 0) {
                if (rollback) { return Optional.empty(); } // Safety, should never happen??
                rollback = true;
            } else {
                var ctr = this.holder[idx];
                if (CollectionUtils.isNotEmpty(ctr)) {
                    return Optional.of(new PastBirthdayCritters(ctr, rollback));
                }
            }
        }
    }

    private int rollingDecrement(int i) {
        i -= 1;
        if (i < 0) { i += this.holder.length; }
        return i;
    }

    public record FutureBirthdayCritters(List<BirthdayCritter> critter, boolean nextYear) {}
    public record PastBirthdayCritters(List<BirthdayCritter> critter, boolean lastYear) {}
}