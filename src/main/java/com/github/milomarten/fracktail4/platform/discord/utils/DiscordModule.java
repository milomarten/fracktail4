package com.github.milomarten.fracktail4.platform.discord.utils;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import org.springframework.stereotype.Component;

@Component
public class DiscordModule extends SimpleModule {
    public DiscordModule() {
        super("DiscordModule", new Version(1, 0, 0, "", null, null));
        addSerializer(new SnowflakeSerializer());
        addDeserializer(Snowflake.class, new SnowflakeDeserializer());
        addSerializer(new ReactionEmojiSerializer());
        addDeserializer(ReactionEmoji.class, new ReactionEmojiDeserializer());
    }
}
