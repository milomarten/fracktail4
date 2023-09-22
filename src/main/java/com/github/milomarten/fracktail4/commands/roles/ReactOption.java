package com.github.milomarten.fracktail4.commands.roles;

import discord4j.core.object.reaction.ReactionEmoji;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
class ReactOption<ID> {
    private ID id;
    private ReactionEmoji emoji;
    private String description;

    public ReactOption(ReactOption<ID> toCopy) {
        this.id = toCopy.id;
        this.emoji = toCopy.emoji;
        this.description = toCopy.description;
    }
}
