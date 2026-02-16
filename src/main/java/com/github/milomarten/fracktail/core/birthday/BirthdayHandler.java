package com.github.milomarten.fracktail.core.birthday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail.core.birthday.v3.EventAndWhen;
import com.github.milomarten.fracktail.core.birthday.v3.ScrollingCalendar;
import com.github.milomarten.fracktail.core.birthday.v3.birthday.CritterBirthday;
import com.github.milomarten.fracktail.core.birthday.v3.birthday.HardCodedBirthday;
import com.github.milomarten.fracktail.core.birthday.v3.birthday.SnowflakeBirthday;
import com.github.milomarten.fracktail.core.persistence.Persistence;
import com.github.milomarten.fracktail.core.persistence.PersistenceBean;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Year;
import java.util.*;

@Component
@RequiredArgsConstructor
public class BirthdayHandler implements PersistenceBean {
    private static final TypeReference<List<BirthdayCritter>> BIRTHDAY_TYPE = new TypeReference<List<BirthdayCritter>>() {};
    public static final String BIRTHDAYS_KEY = "birthdays";

    private final Persistence persistence;
    private final GatewayDiscordClient discordClient;
    private final BirthdayConfiguration birthdayConfiguration;

    Map<Snowflake, EventAndWhen<CritterBirthday>> birthdaysById;
    ScrollingCalendar<CritterBirthday> birthdaysByDate;

    @PostConstruct
    private void initLoad() {
        load().block();
    }

    @Override
    public Mono<Void> load() {
        return persistence.retrieve(BIRTHDAYS_KEY, BIRTHDAY_TYPE)
                .switchIfEmpty(Mono.fromSupplier(List::of))
                .doOnSuccess(birthdays -> {
                    this.birthdaysById = new HashMap<>();
                    this.birthdaysByDate = new ScrollingCalendar<>();
                    birthdays.stream()
                            .map(bc -> new SnowflakeBirthday(bc.getCritter(), bc.getDay(), bc.getYear().orElse(null)))
                            .forEach(sb -> {
                                this.birthdaysByDate.addRecurringEvent(sb);
                                this.birthdaysById.put(
                                        sb.who(),
                                        sb.createForYear(Year.of(2024))
                                );
                            });
                    birthdayConfiguration.getHardCoded()
                            .forEach(hc -> {
                                var aligned = new HardCodedBirthday(hc.getRawName(), hc.getDay(), hc.getYear(), hc.getGuilds());
                                this.birthdaysByDate.addRecurringEvent(aligned);
                            });
                })
                .then();
    }

    @Override
    public Mono<Void> store() {
        var persistObj = this.birthdaysById.values()
                .stream()
                .map(EventAndWhen::event)
                .filter(obj -> obj instanceof SnowflakeBirthday)
                .map(cb -> ((SnowflakeBirthday) cb))
                .map(sb -> {
                    return new BirthdayCritter(sb.who(), sb.when(), sb.yob());
                })
                .toList();
        return this.persistence.store(BIRTHDAYS_KEY, persistObj);
    }

    public List<CritterBirthday> getBirthdaysOn(LocalDate when) {
        return this.birthdaysByDate.getNextEvents(when, true)
                .event();
    }

    public MonthDay getBirthday(Snowflake id) {
        var bday = birthdaysById.get(id);
        if (bday == null) {
            return null;
        }
        return MonthDay.from(bday.when());
    }
}
