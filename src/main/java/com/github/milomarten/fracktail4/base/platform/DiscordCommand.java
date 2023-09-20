package com.github.milomarten.fracktail4.base.platform;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public interface DiscordCommand {
    Mono<?> doCommand(MessageCreateEvent event);
}
