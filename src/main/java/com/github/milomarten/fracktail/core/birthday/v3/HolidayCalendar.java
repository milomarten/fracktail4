package com.github.milomarten.fracktail.core.birthday.v3;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

@Component
public class HolidayCalendar implements GenericCalendar<Holiday> {
    @Override
    public Collection<Holiday> getItemsForDay(LocalDate when) {
        Stream<Holiday> staticHolidays = Arrays.stream(StaticHolidays.values())
                .filter(sh -> MonthDay.from(when).equals(sh.getDayOfCelebration()))
                .map(sh -> sh);

        Stream<Holiday> dynamicHolidays = Arrays.stream(DynamicHolidays.values())
                .filter(dh -> dh.isOnDay(when))
                .map(dh -> dh);

        return Stream.concat(staticHolidays, dynamicHolidays)
                .toList();
    }
}
