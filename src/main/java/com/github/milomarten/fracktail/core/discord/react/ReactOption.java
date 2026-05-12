package com.github.milomarten.fracktail.core.discord.react;

import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactOption<ID> {
    private ID id;
    private ReactionEmoji emoji;
    private String description;

    public ReactOption(ReactOption<ID> toCopy) {
        this.id = toCopy.id;
        this.emoji = toCopy.emoji;
        this.description = toCopy.description;
    }

    public boolean equalsByIdOrReaction(ReactOption<ID> other) {
        return this.id.equals(other.id) || compareByReactionEmoji(other);
    }

    public boolean compareByReactionEmoji(ReactOption<ID> other) {
        var customOne = emoji.asCustomEmoji();
        var customTwo = other.emoji.asCustomEmoji();
        if (customOne.isPresent() && customTwo.isPresent()) {
            return compareReactionEmoji(customOne.get(), customTwo.get());
        } else if (customOne.isEmpty() && customTwo.isEmpty()) {
            return compareReactionEmoji(
                    // should never throw
                    emoji.asUnicodeEmoji().orElseThrow(),
                    other.emoji.asUnicodeEmoji().orElseThrow()
            );
        } else {
            return false;
        }
    }

    private static boolean compareReactionEmoji(ReactionEmoji.Custom one, ReactionEmoji.Custom two) {
        return Objects.equals(one.asFormat(), two.asFormat());
    }

    private static boolean compareReactionEmoji(ReactionEmoji.Unicode one, ReactionEmoji.Unicode two) {
        return Objects.equals(one.getRaw(), two.getRaw());
    }
}
