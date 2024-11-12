package com.github.milomarten.fracktail4.platform.discord.slash;

import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordParameterHelper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import jakarta.validation.ConstraintViolation;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;

@RequiredArgsConstructor
public abstract class AbstractSlashCommand<PARAM> implements SlashCommandWrapper {
    private final DiscordParameterHelper helper;

    public abstract Class<PARAM> getParameterClass();

    @Override
    public final ApplicationCommandRequest getRequest() {
        return augment(ApplicationCommandRequest.builder()
                .addAllOptions(helper.toParameterSpec(getParameterClass()))
        ).build();
    }

    protected abstract ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder);

    @Override
    public final Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return Mono.fromCallable(() -> helper.toParameterObject(event, this.getParameterClass()))
                .map(param -> {
                    var violations = helper.validate(param);
                    if (violations.isEmpty()) {
                        return handleEvent(event, param);
                    } else {
                        return handleValidationErrors(event, violations);
                    }
                })
                .flatMap(response -> response.respond(event));
    }

    protected abstract SlashCommandResponse handleEvent(ChatInputInteractionEvent event, PARAM parameters);

    protected SlashCommandResponse handleValidationErrors(ChatInputInteractionEvent event, Set<ConstraintViolation<PARAM>> errors) {
        return Responses.replyEphemeral(errors.iterator().next().getMessage());
    }
}
