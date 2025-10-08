package com.github.milomarten.fracktail5.platform.util;

import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DiscordVisitor {
    public Visitor<ImmutableApplicationCommandRequest.Builder> name(String name) {
        return input -> input.name(name);
    }

    public Visitor<ImmutableApplicationCommandRequest.Builder> description(String description) {
        return input -> input.description(description);
    }
}
