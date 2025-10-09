package com.github.milomarten.fracktail5.platform.util;

import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
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

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argName(String name) {
        return input -> input.name(name);
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argDescription(String description) {
        return input -> input.description(description);
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argRequired(boolean required) {
        return input -> input.required(required);
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argMinLength(int minLength) {
        return input -> input.minLength(minLength);
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argMaxLength(int maxLength) {
        return input -> input.maxLength(maxLength);
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argMin(Number min) {
        return input -> input.minValue(min.doubleValue());
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argMax(Number max) {
        return input -> input.maxValue(max.doubleValue());
    }
}
