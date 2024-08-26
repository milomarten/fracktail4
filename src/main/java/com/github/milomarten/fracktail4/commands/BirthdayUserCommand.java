package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import com.github.milomarten.fracktail4.platform.discord.slash.UserCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.MonthDay;

@Component
@RequiredArgsConstructor
public class BirthdayUserCommand implements UserCommandWrapper {
    private final BirthdayHandler handler;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("birthday")
                .type(2)
                .build();
    }

    @Override
    public Mono<?> handleEvent(UserInteractionEvent event) {
        var birthdayMaybe = handler.getNextBirthday(event.getTargetId());
        var name = BirthdayUtils.getName(event.getResolvedUser());
        if (birthdayMaybe.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "I don't know when " + name + "'s birthday is!");
        } else {
            var birthday = birthdayMaybe.get();
            var now = LocalDate.now();
            var date = BirthdayUtils.getDisplayBirthday(MonthDay.from(birthday.when()));
            var duration = BirthdayUtils.getDurationWords(now, birthday.when());

            var reply = String.format("%s's birthday is on %s. That's %s!", name, date, duration);
            return SlashCommands.replyEphemeral(event, reply);
        }
    }
}
