package com.github.milomarten.fracktail4.platform.discord.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DiscordJacksonMapper {
    private final ObjectMapper om;

    public JsonNode map(ChatInputInteractionEvent event) {
        return map(event.getOptions());
    }

    private JsonNode map(List<ApplicationCommandInteractionOption> options) {
        var objNode = om.createObjectNode();
        for (var subOption : options) {
            var key = subOption.getName();
            var valueMaybe = subOption.getValue();
            if (valueMaybe.isPresent()) {
                objNode.put(key, valueMaybe.get().getRaw());
            } else {
                objNode.set(key, map(subOption.getOptions()));
            }
        }
        return objNode;
    }

    public <T> T map(ChatInputInteractionEvent event, Class<T> clazz) {
        try {
            return om.treeToValue(map(event), clazz);
        } catch (IllegalArgumentException | JsonProcessingException ex) {
            throw new DiscordMapperException("Unable to parse event to object", ex);
        }
    }
}
