package com.github.milomarten.fracktail.core.birthday.v3.birthday;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.milomarten.fracktail.core.birthday.v3.ScrollingCalendar;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SnowflakeBirthday.class, name = "REGULAR"),
        @JsonSubTypes.Type(value = HardCodedBirthday.class, name = "HARD_CODED")
})
public interface BirthdayCalendarEventGenerator extends ScrollingCalendar.EventGenerator<CritterBirthday> {
}
