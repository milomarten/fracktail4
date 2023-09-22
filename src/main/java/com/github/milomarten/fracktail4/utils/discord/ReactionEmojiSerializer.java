package com.github.milomarten.fracktail4.utils.discord;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import discord4j.core.object.reaction.ReactionEmoji;

import java.io.IOException;

public class ReactionEmojiSerializer extends StdSerializer<ReactionEmoji> {
    public ReactionEmojiSerializer() {
        super(ReactionEmoji.class);
    }

    @Override
    public void serialize(ReactionEmoji value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        var customMaybe = value.asCustomEmoji();
        if (customMaybe.isPresent()) {
            var custom = customMaybe.get();
            gen.writeStartObject();
            gen.writeStringField("type", "custom");
            gen.writeStringField("id", custom.getId().asString());
            gen.writeStringField("name", custom.getName());
            gen.writeBooleanField("isAnimated", custom.isAnimated());
            gen.writeEndObject();
            return;
        }

        var unicodeMaybe = value.asUnicodeEmoji();
        if (unicodeMaybe.isPresent()) {
            var unicode = unicodeMaybe.get();
            gen.writeStartObject();
            gen.writeStringField("type", "unicode");
            gen.writeStringField("raw", unicode.getRaw());
            gen.writeEndObject();
            return;
        }

        gen.writeNull();
    }
}
