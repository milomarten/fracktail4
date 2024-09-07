package com.github.milomarten.fracktail4.birthday.ical;

import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import com.github.milomarten.fracktail4.commands.BirthdaySlashCommand;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.extensions.model.property.WrCalDesc;
import net.fortuna.ical4j.extensions.model.property.WrCalName;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Component
public class BirthdayICalService {
    private final BirthdayHandler birthdayHandler;
    private final GatewayDiscordClient client;

    public Mono<Calendar> createCalendar() {
        return Flux.fromIterable(birthdayHandler.getBirthdays())
                .flatMap(BirthdaySlashCommand::resolve)
                .collectList()
                .map(celebrators -> {
                    var calendar = new Calendar()
                            .withProdId("-//Milo Marten//Fracktail 4.0//EN")
                            .withDefaults()
                            .getFluentTarget();

                    calendar.add(new WrCalName(new ParameterList(), "Fracktail Birthdays"));
                    calendar.add(new WrCalDesc(new ParameterList(), "Birthdays, as generated from Fracktail in Milo's Saloon."));
                    calendar.add(new RefreshInterval(new ParameterList(), Duration.ofHours(12)));

                    celebrators.stream()
                            .<VEvent>map(celebrator -> {
                                var dateOfCelebration = celebrator.getT1().getDayOfCelebration();
                                var username = celebrator.getT2();
                                return new VEvent()
                                        .add(new DtStart<>(dateOfCelebration.atYear(2023)))
                                        .add(new DtEnd<>(dateOfCelebration.atYear(2023).plusDays(1)))
//                                        .add(new Uid(celebrator.getT1().getCritter().asString()))
                                        .add(new Summary(String.format("%s's Birthday", username)))
                                        .add(new RRule<>("FREQ=YEARLY"));
                            })
                            .forEach(calendar::add);

                    return calendar;
                });
    }
}
