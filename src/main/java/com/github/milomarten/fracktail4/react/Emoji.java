package com.github.milomarten.fracktail4.react;

import discord4j.common.util.Snowflake;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Emoji {
    String raw;
    Snowflake guildId;
    Snowflake customId;

    public static Emoji unicode(String raw) {
        return new Emoji(raw, null, null);
    }

    public static Emoji custom(Snowflake guildId, Snowflake customId) {
        return new Emoji(null, guildId, customId);
    }
}
