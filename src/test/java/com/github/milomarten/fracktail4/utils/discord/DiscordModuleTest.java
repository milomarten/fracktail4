package com.github.milomarten.fracktail4.utils.discord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import discord4j.core.object.reaction.ReactionEmoji;
import org.junit.jupiter.api.Test;

class DiscordModuleTest {
    private static final ObjectMapper om = new ObjectMapper()
            .registerModule(new DiscordModule());
    @Test
    public void serializeUnicode() throws JsonProcessingException {
        var emote = ReactionEmoji.unicode("❤️");
        var string = om.writeValueAsString(emote);

        System.out.println(string);
    }

    @Test
    public void deserializeUnicode() throws JsonProcessingException {
        var string = "{\"type\":\"unicode\", \"raw\":\"❤️\"}";
        var emote = om.readValue(string, ReactionEmoji.class);

        System.out.println(emote);
    }
}