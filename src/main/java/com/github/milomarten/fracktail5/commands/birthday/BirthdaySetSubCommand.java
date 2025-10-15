package com.github.milomarten.fracktail5.commands.birthday;

import com.github.milomarten.fracktail.core.birthday.BirthdayHandler;
import com.github.milomarten.fracktail.core.job.BirthdayJob;
import com.github.milomarten.fracktail.core.birthday.BirthdayUtils;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.ContextParameter;
import com.github.milomarten.fracktail5.platform.discord.argument.EnumStringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.IntArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.object.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DateTimeException;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;

@Component
@RequiredArgsConstructor
public class BirthdaySetSubCommand {
    private final BirthdayHandler handler;
    private final BirthdayJob job;

    @Getter
    private final DiscordSubCommand.Details details = new DiscordArgumentSubCommandDetails<>(
                "set",
                "Add your birthday to the birthday calendar",
                new PojoParser<>(Parameters::new)
                        .addField(new ContextParameter<>(InteractionCreateEvent::getUser), Parameters::setUser)
                        .addField(new EnumStringArgumentParser<>(
                                "month",
                                "Month of birth",
                                Month.class,
                                BirthdayUtils::getDisplayMonth
                        ).required(), Parameters::setMonth)
                        .addField(new IntArgumentParser(
                                "day",
                                "Day of birth"
                        ).min(1).max(31).required(), Parameters::setDay)
                        .addField(new IntArgumentParser(
                                "year",
                                "Year of birth"
                        ).min(1900).max(Year.now().getValue())
                                .defaultToNull()
                                .map(i -> i == null ? null : Year.of(i)), Parameters::setYear),
                this::setBirthday
        );

    private DiscordResponse setBirthday(Parameters birthday) {
        if (handler.hasBirthday(birthday.getUser().getId())) {
            return DiscordResponses.replyEphemeral("You've already entered your birthday!");
        }

        MonthDay day;
        try {
            day = MonthDay.of(birthday.getMonth(), birthday.getDay());
        }  catch (DateTimeException ex) {
            return DiscordResponses.replyEphemeral(birthday.getMonth() + "/" + birthday.getDay() + " is not a valid day! Nice try!");
        }

        var response = handler.createBirthday(
                birthday.getUser().getId(),
                day, birthday.getYear()
        ).thenReturn("Added your birthday to the calendar!");

        job.checkBirthdayAndAnnounceIfNecessary(birthday.getUser().getId());
        return DiscordResponses.delayedResponse(response);
    }

    @Data
    private static class Parameters {
        User user;
        Month month;
        int day;
        Year year;
    }
}
