package com.github.milomarten.fracktail4.utils.discord;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import discord4j.common.util.Snowflake;

import java.io.IOException;

public class SnowflakeDeserializer extends StdDeserializer<Snowflake> {
    public SnowflakeDeserializer() {
        super(Snowflake.class);
    }

    @Override
    public Snowflake deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        var value = p.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }
        return Snowflake.of(value);
    }
}
