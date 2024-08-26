package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.birthday.BirthdayHandler;
import com.github.milomarten.fracktail4.birthday.BirthdayUtils;
import com.github.milomarten.fracktail4.platform.discord.slash.UserCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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
        var targetName = event.getResolvedUser().getGlobalName()
                .orElseGet(() -> event.getResolvedUser().getUsername());
        var birthdayMaybe = handler.getBirthday(event.getTargetId());
        if (birthdayMaybe.isEmpty()) {
            String fStr = String.format("I don't know %s' birthday, unfortunately", targetName);
            return SlashCommands.replyEphemeral(event, fStr);
        } else {
            var birthday = birthdayMaybe.get();
            String dayOf = BirthdayUtils.getDisplayBirthday(birthday.getDay());
            String fStr = String.format("%s' birthday is on %s!", targetName, dayOf);
            return SlashCommands.replyEphemeral(event, fStr);
        }
    }
}
