package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail.core.discord.react.ReactMessage;
import com.github.milomarten.fracktail.core.discord.react.ReactOption;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordNoArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class ViewBranch {
    public static DiscordNoArgumentSubCommandDetails details(RoleReactCommand parent) {
        return new DiscordNoArgumentSubCommandDetails(
                "view",
                "See the changes that will be pushed on next publish",
                () -> view(parent)
        );
    }

    public static DiscordResponse view(RoleReactCommand parent) {
        return parent.updateContents(oven -> {
            if (oven.isNew()) {
                return DiscordResponses.replyEphemeral(
                      describeNew(oven)
                );
            } else {
                var message = parent.getReacts().getRoleReactMessages()
                        .stream()
                        .filter(rm -> Objects.equals(oven.getMessageId(), rm.getMessageId()))
                        .findFirst()
                        .map(old -> describeExisting(oven, old))
                        .orElseGet(() -> describeNew(oven));

                return DiscordResponses.replyEphemeral(message);
            }
        });
    }

    private static String describeNew(ReactMessage<Snowflake> building) {
        var sj = new StringJoiner("\n");
        sj.add("**Description:** " + building.getDescription());
        if (!building.hasNoOptions()) {
            sj.add("**Options:**");
            for (var option : building.getOptions()) {
                sj.add("- " + option.getEmoji() + " = " + option.getDescription());
            }
        }
        return sj.toString();
    }

    public static String describeExisting(ReactMessage<Snowflake> building, ReactMessage<Snowflake> existing) {
        var sj = new StringJoiner("\n");
        if (!Objects.equals(building.getDescription(), existing.getDescription())) {
            sj.add("**Description:** " + building.getDescription());
        }

        var compare = compare(existing.getOptions(), building.getOptions());
        if (!compare.isEmpty()) {
            sj.add("**Options:**");
            for (var c : compare) {
                var left = c.left;
                var right = c.right;
                if (left != null && right != null) {
                    sj.add("- **Update:** " + left.getEmoji() + " = " + left.getDescription() + " -> " + right.getEmoji() + " = " + right.getDescription());
                } else if (left == null) {
                    sj.add("- **Add:** "+ right.getEmoji() + " = " + right.getDescription());
                } else {
                    sj.add("- **Remove:** "+ left.getEmoji() + " = " + left.getDescription());
                }
            }
        }

        if (sj.length() == 0) {
            return "No changes";
        } else {
            return sj.toString();
        }
    }

    private static List<Comparison<ReactOption<Snowflake>>> compare(List<ReactOption<Snowflake>> left, List<ReactOption<Snowflake>> right) {
        var comparisons = new ArrayList<Comparison<ReactOption<Snowflake>>>();
        int maxSize = Math.max(left.size(), right.size());
        for (var i = 0; i < maxSize; i++) {
            var l = i < left.size() ? left.get(i) : null;
            var r = i < right.size() ? right.get(i) : null;
            if (l == null || r == null || !Objects.equals(l.getId(), r.getId()) || !l.compareByReactionEmoji(r)) {
                comparisons.add(new Comparison<>(l, r));
            }
        }
        return comparisons;
    }

    private record Comparison<T>(T left, T right) {}
}
