package com.github.milomarten.fracktail.core.birthday.v3;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.ArrayList;
import java.util.Collection;

public class MonthDayCalendar<T> implements GenericCalendar<T> {
    private final MultiValuedMap<MonthDay, T> items;

    public MonthDayCalendar() {
        this.items = new HashSetValuedHashMap<>();
    }

    public void addEvent(MonthDay when, T item) {
        this.items.put(when, item);
    }

    public void removeEvent(MonthDay when, T item) {
        this.items.removeMapping(when, item);
    }

    @Override
    public Collection<T> getItemsForDay(LocalDate when) {
        return new ArrayList<>(items.get(MonthDay.from(when)));
    }
}
