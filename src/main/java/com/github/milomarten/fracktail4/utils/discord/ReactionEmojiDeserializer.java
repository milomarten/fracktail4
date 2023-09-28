package com.github.milomarten.fracktail4.utils.discord;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;

import java.io.IOException;

public class ReactionEmojiDeserializer extends StdDeserializer<ReactionEmoji> {
    public ReactionEmojiDeserializer() {
        super(ReactionEmoji.class);
    }

    @Override
    public ReactionEmoji deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        var node = ctxt.readTree(p);
        String type = node.get("type").asText();
        return switch (type) {
            case "unicode" -> ReactionEmoji.unicode(node.get("raw").asText());
            case "custom" -> ReactionEmoji.custom(
                        Snowflake.of(node.get("id").asText()),
                        node.get("name").asText(),
                        node.get("isAnimated").asBoolean()
                );
            default -> null;
        };
    }
}
