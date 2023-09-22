package com.github.milomarten.fracktail4.commands.roles;

import discord4j.core.object.reaction.ReactionEmoji;
import lombok.Data;

@Data
class ReactOption<ID> {
    private ID id;
    private ReactionEmoji emoji;
    private String description;
}
