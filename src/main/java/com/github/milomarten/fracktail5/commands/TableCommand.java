package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.dice.table.Rolltables;
import com.github.milomarten.fracktail.core.dice.table.multi.MultiRollParams;
import com.github.milomarten.fracktail5.platform.discord.DiscordArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.argument.*;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.Data;
import org.apache.commons.rng.RestorableUniformRandomProvider;
import org.apache.commons.rng.simple.RandomSource;
import org.springframework.stereotype.Component;

@Component
public class TableCommand implements DiscordSlashCommand {
    private static final RestorableUniformRandomProvider RANDOMNESS = RandomSource.MT_64.create();

    private final Details DISCORD_SPEC = new DiscordArgumentDetails<>(
            "table",
            "Select a random item from one of our tables",
            new PojoParser<>(TableCommand.Arguments::new)
                    .addField(new StringArgumentParser(
                                    "table",
                                    "The table to pull from")
                                    .required(),
                            TableCommand.Arguments::setTable
                    )
                    .addField(new StringArgumentParser(
                                    "comment",
                                    "A small description of the roll")
                                    .defaultTo(null),
                            TableCommand.Arguments::setComment
                    )
                    .addField(new BooleanArgumentParser(
                                    "visible",
                                    "Whether this role should be visible to all")
                                    .defaultTo(true),
                            TableCommand.Arguments::setVisible
                    )
                    .addField(new IntArgumentParser(
                                    "quantity",
                                    "The number of options to get")
                                    .min(0).max(15).defaultTo(1),
                            TableCommand.Arguments::setQuantity
                    )
                    .addField(new BooleanArgumentParser(
                                    "distinct",
                                    "Whether the results from each roll should be unique between each other")
                            .defaultTo(false),
                            TableCommand.Arguments::setDistinct
                    )
            ,
            this::roll
    );

    @Override
    public Details getDiscordSlashCommandDetails() {
        return DISCORD_SPEC;
    }

    private DiscordResponse roll(Arguments args) {
        var roll = Rolltables.rollMultitable(args.table, MultiRollParams.builder()
                        .distinct(args.distinct)
                        .quantity(args.quantity)
                        .random(RANDOMNESS)
                .build());
        if (roll == null) {
            return DiscordResponses.replyEphemeral("Table " + args.table + " has no data.");
        } else {
            String response;
            if (args.comment == null) {
                response = String.format("%s -> **%s**", args.table, roll);
            } else {
                response = String.format("%s\n%s -> **%s**", args.comment, args.table, roll);
            }

            return args.visible ? DiscordResponses.reply(response) : DiscordResponses.replyEphemeral(response);
        }
    }

    @Data
    private static class Arguments {
        private String table;
        private String comment;
        private boolean visible;
        private int quantity;
        private boolean distinct;
    }
}
