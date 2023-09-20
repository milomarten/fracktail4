package com.github.milomarten.fracktail4.base;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Set;

public class SimplePrefixedCommand extends AbstractPrefixedCommand {
    private final CommandData commandData;
    private final String response;

    public SimplePrefixedCommand(String command, String response, String... aliases) {
        this.commandData = CommandData.builder()
                .id(command)
                .alias(command)
                .aliases(Set.of(aliases))
                .description(response)
                .build();
        this.response = response;
    }

    @Override
    protected Mono<?> doCommand(MessageCreateEvent event) {
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(this.response));
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }
}
