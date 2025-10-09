package com.github.milomarten.fracktail5.platform.util;

import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordResponses {
    public DiscordResponse reply(String message) {
        return event -> event.reply(message);
    }

    public DiscordResponse replyEphemeral(String message) {
        return event -> event.reply(message).withEphemeral(true);
    }
}
