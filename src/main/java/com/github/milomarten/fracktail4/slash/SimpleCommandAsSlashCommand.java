package com.github.milomarten.fracktail4.slash;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SimpleCommandAsSlashCommand implements SlashCommandWrapper {
    private final SimpleCommand simpleCommand;

    @Override
    public ApplicationCommandRequest getRequest() {
        var commandData = simpleCommand.getCommandData();
        return ApplicationCommandRequest.builder()
                .name(commandData.getId())
                .description(commandData.getDescription())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return event.reply(simpleCommand.getResponse());
    }
}
