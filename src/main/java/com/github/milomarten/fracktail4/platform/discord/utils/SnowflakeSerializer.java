package com.github.milomarten.fracktail4.platform.discord.utils;

import com.fasterxml.jackson.core.JsonGenerator;
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
}
