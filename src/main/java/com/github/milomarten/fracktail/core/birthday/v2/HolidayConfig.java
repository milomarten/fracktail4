package com.github.milomarten.fracktail.core.birthday.v2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HolidayConfig {
    @Bean
    public EventCalendar<StaticHolidays> holidayCalendar() {
        var ec = new EventCalendar<StaticHolidays>();
        for (var holiday : StaticHolidays.values()) {
            ec.addEvent(holiday);
        }
        return ec;
    }
}
