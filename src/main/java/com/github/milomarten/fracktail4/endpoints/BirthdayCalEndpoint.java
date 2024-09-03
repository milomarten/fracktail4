package com.github.milomarten.fracktail4.endpoints;

import com.github.milomarten.fracktail4.birthday.ical.BirthdayICalCacheJob;
import lombok.RequiredArgsConstructor;
import net.fortuna.ical4j.data.CalendarOutputter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/birthday")
public class BirthdayCalEndpoint {
    private final BirthdayICalCacheJob iCalService;

    @GetMapping(
            value = "/calendar.ics",
            produces = "text/calendar"
    )
    public byte[] getCalendarFile() throws IOException {
        var outputter = new CalendarOutputter();
        var sw = new ByteArrayOutputStream();

        var calendar = iCalService.getCurrentCalendar();
        outputter.output(calendar, sw);
        return sw.toByteArray();
    }
}
