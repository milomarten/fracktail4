package com.github.milomarten.fracktail5.platform.discord.util;

import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;

@UtilityClass
public class DiscordVisitor {
    public Visitor<ImmutableApplicationCommandRequest.Builder> name(String name) {
        return input -> input.name(name);
    }

    public Visitor<ImmutableApplicationCommandRequest.Builder> description(String description) {
        return input -> input.description(description);
    }

    public Visitor<ImmutableApplicationCommandRequest.Builder> permission(Permission... permissions) {
        return input -> {
            var computed = BigInteger.ZERO;
            for (var permission : permissions) {
                computed = computed.or(permission.getRaw());
            }
            return input.defaultMemberPermissions(computed.toString());
        };
    }

    public Visitor<ImmutableApplicationCommandOptionData.Builder> argType(ApplicationCommandOption.Type type) {
        return input -> input.type(type.getValue());
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
