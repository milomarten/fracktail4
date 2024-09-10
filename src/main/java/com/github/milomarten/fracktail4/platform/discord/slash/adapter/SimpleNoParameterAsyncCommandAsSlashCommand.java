package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import com.github.milomarten.fracktail4.base.SimpleNoParameterAsyncCommand;
import com.github.milomarten.fracktail4.base.SimpleNoParameterCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SimpleNoParameterAsyncCommandAsSlashCommand implements SlashCommandWrapper {
    private final SimpleNoParameterAsyncCommand wrapper;

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
                .flatMap(event::createFollowup);
    }
}
