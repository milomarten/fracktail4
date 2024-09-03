package com.github.milomarten.fracktail4.birthday.ical;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.model.Calendar;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class BirthdayICalCacheJob {
    private final BirthdayICalService service;

    @Getter private Calendar currentCalendar;

    @PostConstruct
    private void setUp() {
        //Initialize the first one!
        this.currentCalendar = this.service
                .createCalendar()
                .blockOptional(Duration.ofMinutes(2))
                .orElseThrow(() -> new IllegalArgumentException("Unable to create calendar"));
    }

    // This runs at midnight every night.
    // But it can also be triggered manually.
    // Right now, it is triggered on create or delete of a birthday.
    @Scheduled(cron = "@midnight")
    public void updateCalendar() {
        this.service.createCalendar()
                .subscribe(cal -> this.currentCalendar = cal);
    }
}
