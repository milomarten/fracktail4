package com.github.milomarten.fracktail4.commands.dnd;

import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.Optional;

public enum Type {
    NONE,
    NORMAL, FIGHTING, FLYING, POISON,
    GROUND, ROCK, BUG, GHOST, STEEL,
    FIRE, WATER, GRASS, ELECTRIC,
    PSYCHIC, ICE, DARK, DRAGON, FAIRY;

    public static Iterable<ApplicationCommandOptionChoiceData> asChoices() {
        return Arrays.stream(Type.values())
                .map(t -> ApplicationCommandOptionChoiceData.builder()
                        .name(t.name())
                        .value(t.name())
                        .build()
                )
                .map(t -> (ApplicationCommandOptionChoiceData)t)
                .toList();
    }

    public static Optional<Type> fromString(String s) {
        return Optional.ofNullable(EnumUtils.getEnumIgnoreCase(Type.class, s, null));
    }
}
