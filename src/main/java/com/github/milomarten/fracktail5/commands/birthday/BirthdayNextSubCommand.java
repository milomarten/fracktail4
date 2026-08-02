package com.github.milomarten.fracktail5.commands.birthday;

import com.github.milomarten.fracktail.core.birthday.BirthdayHandler;
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
public class BirthdayNextSubCommand {
    private final BirthdayHandler handler;

    @Getter
    private final DiscordSubCommand.Details details = new DiscordArgumentSubCommandDetails<>(
            "next",
            "Get the next birthday",
            new ContextParameter<>(a -> a.getInteraction().getGuildId().orElse(null)),
            this::getNextBirthday
    );

    private DiscordResponse getNextBirthday(Snowflake guildId) {
        var now = LocalDate.now();
        return DiscordResponses.delayedResponse(next(guildId, now, now));
    }

    private Mono<String> next(Snowflake guildId, LocalDate searchPoint, LocalDate firstSearchPoint) {
        // If our search has crossed an entire year, there is no point continuing on.
        var timeBetweenSearchPoints = Period.between(firstSearchPoint, searchPoint);
        if (timeBetweenSearchPoints.getYears() >= 1) {
            return Mono.just("There are no birthdays in the calendar...");
        }
        var nextBirthdaysMaybe = handler.getNextBirthdays(searchPoint);
        if (nextBirthdaysMaybe.isEmpty()) {
            return Mono.just("There are no birthdays in the calendar...");
        }
        var nextBirthdays = nextBirthdaysMaybe.get();
        var nextBirthdayCritters = nextBirthdays.item();
        return Flux.fromIterable(nextBirthdayCritters)
                .filterWhen(bei -> bei.shouldDisplayForGuild(guildId))
                .flatMap(BirthdayEventInstance::resolve)
                .collectList()
                .flatMap(birthdays -> {
                    if (birthdays.isEmpty()) {
                        // Recursively call for next birthdays, starting at the day of the last retrieved birthdays
                        return next(guildId, nextBirthdays.when(), firstSearchPoint);
                    } else {
                        var dateOfBirthdays = birthdays.get(0).getT1().getDayOfCelebration();

                        var title = String.format("The next birthday(s) are on %s (%s):\n",
                                BirthdayUtils.getDisplayBirthday(dateOfBirthdays),
                                BirthdayUtils.getDurationWords(LocalDate.now(), nextBirthdays.when()));
                        var reply = birthdays.stream()
                                .map(t -> "- " + t.getT2())
                                .collect(Collectors.joining("\n", title, ""));

                        return Mono.just(reply);
                    }
                });
    }
}
