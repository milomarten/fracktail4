package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.publisher.Mono;

import java.util.Set;

public class SimpleCommand implements Command, DiscordCommand {
    private final CommandData commandData;
    private final String response;

    public SimpleCommand(String command, String response, String... aliases) {
        this.commandData = CommandData.builder()
                .id(command)
                .alias(command)
                .aliases(Set.of(aliases))
                .description(response)
                .build();
        this.response = response;
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, MessageCreateEvent event) {
        return event.getMessage()
                .getChannel()
                .flatMap(mc -> mc.createMessage(this.response));
    }

    @Override
    public CommandData getCommandData() {
        return this.commandData;
    }
}
