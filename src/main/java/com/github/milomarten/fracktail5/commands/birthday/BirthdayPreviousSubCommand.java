package com.github.milomarten.fracktail5.commands.birthday;

import com.github.milomarten.fracktail.core.birthday.v2.BirthdayHandler;
import com.github.milomarten.fracktail.core.birthday.BirthdayUtils;
import com.github.milomarten.fracktail.core.birthday.v2.BirthdayEventInstance;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.ContextParameter;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BirthdayPreviousSubCommand {
    private final BirthdayHandler handler;

    @Getter
    private final DiscordSubCommand.Details details = new DiscordArgumentSubCommandDetails<>(
            "previous",
            "Get the previously celebrated birthday",
            new ContextParameter<>(a -> a.getInteraction().getGuildId().orElse(null)),
            this::getPreviousBirthday
    );

    private DiscordResponse getPreviousBirthday(Snowflake guildId) {
        var now = LocalDate.now();
        return DiscordResponses.delayedResponse(previous(guildId, now, now));
    }

    private Mono<String> previous(Snowflake guildId, LocalDate searchPoint, LocalDate firstSearchPoint) {
        var timeBetweenSearchPoints = Period.between(firstSearchPoint, searchPoint);
        if (timeBetweenSearchPoints.getYears() <= -1) {
            return Mono.just("There are no birthdays in the calendar...");
        }
        var previousBirthdaysMaybe = handler.getPreviousBirthdays(searchPoint);
        if (previousBirthdaysMaybe.isEmpty()) {
            return Mono.just( "There are no birthdays in the calendar...");
        }
        var previousBirthdays = previousBirthdaysMaybe.get();
        var previousBirthdayCritters = previousBirthdays.celebrators();
        return Flux.fromIterable(previousBirthdayCritters)
                .filterWhen(bei -> bei.shouldDisplayForGuild(guildId))
                .flatMap(BirthdayEventInstance::resolve)
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively try birthdays and purging non-member birthdays
                        return previous(guildId, previousBirthdays.when(), firstSearchPoint);
                    } else {
                        var dateOfBirthdays = birthdays.get(0).getT1().getDayOfCelebration();

                        var title = String.format("The previous birthdays were on %s (%s):\n",
                                BirthdayUtils.getDisplayBirthday(dateOfBirthdays),
                                BirthdayUtils.getDurationWords(LocalDate.now(), previousBirthdays.when()));
                        var reply = birthdays.stream()
                                .map(t -> "- " + t.getT2())
                                .collect(Collectors.joining("\n", title, ""));

                        return Mono.just(reply);
                    }
                });
    }
}
