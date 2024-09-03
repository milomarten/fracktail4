package com.github.milomarten.fracktail4.birthday.ical;

import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@RequiredArgsConstructor
@Slf4j
@Component
public class BirthdayICalService {
    private final BirthdayHandler birthdayHandler;
    private final GatewayDiscordClient client;

    public Mono<Calendar> createCalendar() {
        return Flux.fromIterable(birthdayHandler.getBirthdays())
                .flatMap(bc -> client.getUserById(bc.getCritter())
                        .map(user -> Tuples.of(bc, user.getUsername())))
                .onErrorResume(ex -> {
                    log.error("Error pulling user. Ignoring...", ex);
                    return Mono.empty();
                })
                .collectList()
                .map(celebrators -> {
                    var calendar = new Calendar()
                            .withProdId("-//Milo Marten//Fracktail 4.0//EN")
                            .withDefaults()
                            .getFluentTarget();

                    celebrators.stream()
                            .<VEvent>map(celebrator -> {
                                var dateOfCelebration = celebrator.getT1().getDay();
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
