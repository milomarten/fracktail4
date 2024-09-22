package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import com.github.milomarten.fracktail4.base.CommandOutputFinalizer;
import com.github.milomarten.fracktail4.base.SimpleNoParameterCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@AllArgsConstructor
public class SimpleNoParameterCommandAsSlashCommand implements SlashCommandWrapper {
    private final SimpleNoParameterCommand wrapper;
    private CommandOutputFinalizer<? super ChatInputInteractionEvent, String> finalizer = CommandOutputFinalizer.getDefault();

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name(wrapper.getName())
                .description(wrapper.getDescription())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return event.reply(
                finalizer.mapResponse(event, wrapper.getResponse()));
    }
}
