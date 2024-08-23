package com.github.milomarten.fracktail4.platform.discord.slash;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * A wrapper to convert a SimpleCommand for Slash Commands.
 */
@RequiredArgsConstructor
public class SimpleCommandAsSlashCommand implements SlashCommandWrapper {
    private final SimpleCommand simpleCommand;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name(simpleCommand.command())
                .description(simpleCommand.description())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return event.reply(simpleCommand.response());
    }
}
