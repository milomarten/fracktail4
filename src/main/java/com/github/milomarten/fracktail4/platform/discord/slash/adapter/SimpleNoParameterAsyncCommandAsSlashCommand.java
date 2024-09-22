package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import com.github.milomarten.fracktail4.base.CommandOutputFinalizer;
import com.github.milomarten.fracktail4.base.SimpleNoParameterAsyncCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@AllArgsConstructor
public class SimpleNoParameterAsyncCommandAsSlashCommand implements SlashCommandWrapper {
    private final SimpleNoParameterAsyncCommand wrapper;
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
        return event.deferReply()
                .then(wrapper.getResponse())
                .map(response -> this.finalizer.mapResponse(event, response))
                .flatMap(event::createFollowup);
    }
}
