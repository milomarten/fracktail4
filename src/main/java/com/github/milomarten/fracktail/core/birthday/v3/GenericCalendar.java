package com.github.milomarten.fracktail.core.birthday.v3;

import java.time.LocalDate;
import java.util.Collection;

public interface GenericCalendar<T> {
    Collection<T> getItemsForDay(LocalDate when);
}
