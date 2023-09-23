package com.github.milomarten.fracktail4.base;

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.Data;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.regex.Pattern;

@Data
public class Parameters {
    private static final Pattern CUSTOM_EMOTE_PATTERN = Pattern.compile("<(a)?:([^:]+):([0-9]+)>");
    private static final Pattern ROLE_MENTION_PATTERN = Pattern.compile("<@&([0-9]+)>");
    private final String[] sourceParams;

    public int getNumberOfParameters() { return this.sourceParams.length; }

    public Parameters range(int start) {
        int newLength = sourceParams.length - start;
        if (newLength < 0) {
            throw new IndexOutOfBoundsException(start);
        }
        String[] newParams = new String[newLength];
        System.arraycopy(this.sourceParams, start, newParams, 0, newLength);
        return new Parameters(newParams);
    }

    public Optional<String> getParameter(int idx) {
        if (idx < 0) {
            idx = getNumberOfParameters() + idx;
        }
        if (idx >= 0 && idx < getNumberOfParameters()) {
            return Optional.of(sourceParams[idx]);
        } else {
            return Optional.empty();
        }
    }

    public OptionalInt getIntParameter(int idx) {
        var param = getParameter(idx);
        return param.map(s -> OptionalInt.of(Integer.parseInt(s)))
                .orElseGet(OptionalInt::empty);
    }

    public Optional<Snowflake> getSnowflake(int idx) {
        return getParameter(idx).map(s -> {
            try {
                return Snowflake.of(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        });
    }

    public Optional<Snowflake> getRoleMention(int idx) {
        return getParameter(idx).map(s -> {
            var matcher = ROLE_MENTION_PATTERN.matcher(s);
            if (matcher.matches()) {
                return Snowflake.of(matcher.group(1));
            } else {
                return null;
            }
        });
    }

    public Optional<ReactionEmoji> getEmoji(int idx) {
        var param = getParameter(idx);
        return param.map(s -> {
            var matcher = CUSTOM_EMOTE_PATTERN.matcher(s);
            if (matcher.matches()) {
                var animated = matcher.group(1) != null;
                var name = matcher.group(2);
                var snowflake = Snowflake.of(matcher.group(3));
                return ReactionEmoji.custom(snowflake, name, animated);
            }

            return ReactionEmoji.unicode(s);
        });
    }
}
