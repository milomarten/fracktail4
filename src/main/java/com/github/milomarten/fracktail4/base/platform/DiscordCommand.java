package com.github.milomarten.fracktail4.base.platform;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import reactor.core.publisher.Mono;

public interface DiscordCommand {
    Mono<?> doCommand(MessageCreateEvent event);

    default Mono<Message> respondWith(MessageCreateEvent event, String message) {
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(message));
    }

    default Mono<Message> respondWithDM(MessageCreateEvent event, String message) {
        return Mono.justOrEmpty(event.getMessage().getAuthor())
                .flatMap(User::getPrivateChannel)
                .flatMap(pc -> pc.createMessage(message));
    }
}
