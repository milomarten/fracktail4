package com.github.milomarten.fracktail.core.discord;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import discord4j.common.util.Snowflake;

import java.io.IOException;

public class SnowflakeSerializer extends StdSerializer<Snowflake> {
    public SnowflakeSerializer() {
        super(Snowflake.class);
    }

    @Override
    public void serialize(Snowflake value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(value.asString());
    }

    public static class Key extends JsonSerializer<Snowflake> {

        @Override
        public void serialize(Snowflake snowflake, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeFieldName(snowflake.asString());
        }
    }
}
