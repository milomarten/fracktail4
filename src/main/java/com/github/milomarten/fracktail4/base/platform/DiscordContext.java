package com.github.milomarten.fracktail4.base.platform;

import com.github.milomarten.fracktail4.base.Context;
import com.github.milomarten.fracktail4.permissions.Role;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DiscordContext implements Context {
    private final MessageCreateEvent event;
    @Getter private final Role role;

    @Override
    public Mono<?> respond(String response) {
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(response));
    }

    @Override
    public Mono<?> respondPrivately(String response) {
        return Mono.justOrEmpty(event.getMessage().getAuthor())
                .flatMap(User::getPrivateChannel)
                .flatMap(pc -> pc.createMessage(response));
    }
}
