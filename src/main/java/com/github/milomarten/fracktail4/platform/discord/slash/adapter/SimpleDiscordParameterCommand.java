package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordJacksonMapper;
import com.github.milomarten.fracktail4.platform.discord.mapper.ObjectToDiscordReflectiveMapper;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public abstract class SimpleDiscordParameterCommand<PARAM> implements SlashCommandWrapper {
    private final Class<PARAM> paramClass;
    private final DiscordJacksonMapper discordJacksonMapper;
    private final ObjectToDiscordReflectiveMapper objectToDiscordReflectiveMapper;
    private final Validator validator;

    @Override
    public final ApplicationCommandRequest getRequest() {
        return augmentRequest(ApplicationCommandRequest.builder()
                .addAllOptions(objectToDiscordReflectiveMapper.toParams(paramClass))
        ).build();
    }

    protected abstract ImmutableApplicationCommandRequest.Builder augmentRequest(ImmutableApplicationCommandRequest.Builder builder);

    @Override
    public final Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return Mono.fromCallable(() -> discordJacksonMapper.map(event, paramClass))
        .flatMap(p -> {
            var violations = this.validator.validate(p, this.getClass());
            if (violations.isEmpty()) {
                return handleEvent(p).respond(event);
            } else {
                return handleConstraintViolations(violations).respond(event);
            }
        });
    }

    protected abstract SlashCommandResponse handleEvent(PARAM param);
    protected abstract SlashCommandResponse handleConstraintViolations(Set<ConstraintViolation<PARAM>> violations);
}
