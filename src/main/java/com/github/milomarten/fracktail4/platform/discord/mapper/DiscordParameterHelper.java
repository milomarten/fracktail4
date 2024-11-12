package com.github.milomarten.fracktail4.platform.discord.mapper;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DiscordParameterHelper {
    private final DiscordJacksonMapper djm;
    private final ObjectToDiscordReflectiveMapper drm;
    private final Validator validator;

    public <T> T toParameterObject(ChatInputInteractionEvent event, Class<T> clazz) {
        return djm.map(event, clazz);
    }

    public List<ApplicationCommandOptionData> toParameterSpec(Class<?> clazz) {
        return drm.toParams(clazz);
    }

    public <T> Set<ConstraintViolation<T>> validate(T obj) {
        return validator.validate(obj);
    }
}
