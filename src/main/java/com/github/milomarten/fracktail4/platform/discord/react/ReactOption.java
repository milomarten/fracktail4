package com.github.milomarten.fracktail4.platform.discord.react;

import discord4j.core.object.reaction.ReactionEmoji;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
