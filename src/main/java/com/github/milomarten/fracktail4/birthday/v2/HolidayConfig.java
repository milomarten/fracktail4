package com.github.milomarten.fracktail4.birthday.v2;

import com.github.milomarten.fracktail4.birthday.EventCalendar;
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
