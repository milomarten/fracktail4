package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

public abstract class NormalCommand implements Command, DiscordCommand {
    @Override
    public Mono<?> doCommand(Parameters parameters, MessageCreateEvent event) {
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(doCommand(parameters)));
    }

    protected abstract String doCommand(Parameters params);
}
