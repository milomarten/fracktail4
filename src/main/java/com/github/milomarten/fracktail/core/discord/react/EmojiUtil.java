package com.github.milomarten.fracktail.core.discord.react;

import discord4j.core.object.reaction.ReactionEmoji;

public class EmojiUtil {
    public static ReactionEmoji toEmoji(String input) {
        // tbd - Make this more intelligent
        return ReactionEmoji.unicode(input);
    }
}
