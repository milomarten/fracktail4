package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail.core.dice.StringDiceExpressionEvaluator;
import com.github.milomarten.fracktail.core.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail5.platform.discord.DiscordArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.argument.*;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;

@RequiredArgsConstructor
@Component
public class DiceSlashCommand implements DiscordSlashCommand {
    private final StringDiceExpressionEvaluator evaluator;

    private final Details DISCORD_SPEC = new DiscordArgumentDetails<>(
            "roll",
            "Roll some dice! See https://milomarten.github.io/fracktail/dice for more.",
            new PojoParser<>(Arguments::new)
                    .addField(new StringArgumentParser(
                                    "expression",
                                    "The roll expression to evaluate.")
                                    .required(),
                            Arguments::setExpression
                    )
                    .addField(new StringArgumentParser(
                                    "comment",
                                    "A small description of the roll")
                                    .defaultTo(null),
                            Arguments::setComment
                    )
                    .addField(new BooleanArgumentParser(
                                    "visible",
                                    "Whether this role should be visible to all")
                                    .defaultTo(true),
                            Arguments::setVisible
                    )
                    .addField(new IntArgumentParser(
                                    "scale",
                                    "The number of decimal digits in the output")
                                    .min(0).max(9).defaultTo(0),
                            Arguments::setScale
                    )
                    .addField(new EnumStringArgumentParser<>(
                                    "roundingmode",
                                    "The rounding function for your system",
                                    RoundingOption.class,
                                    RoundingOption::getName)
                                    .defaultTo(RoundingOption.DOWN),
                            Arguments::setRoundingMode
                    )
            ,
            this::roll
    );

    public Details getDiscordSlashCommandDetails() {
        return DISCORD_SPEC;
    }

    private DiscordResponse roll(Arguments parameters) {
        try {
            var roundingMode = parameters.roundingMode.mode;
            var result = evaluator.evaluate(parameters.expression, DiceEvaluatorOptions.builder()
                    .roundingMode(roundingMode)
                    .build());
            String str;
            if (result.value() == null) {
                str = String.format("%s", result.representation());
            } else {
                str = String.format("%s = **%s**", result.representation(),
                        result.valueAsRoundedString(parameters.scale, roundingMode));
            }

            if (StringUtils.isNotBlank(parameters.comment)) {
                str = parameters.comment + "\n" + str;
            }

            if (parameters.visible) {
                return DiscordResponses.reply(str);
            } else {
                return DiscordResponses.replyEphemeral(str);
            }
        } catch (ExpressionSyntaxError ex) {
            return DiscordResponses.replyEphemeral(ex.getMessage());
        }
    }

    @RequiredArgsConstructor
    private enum RoundingOption {
        FLOOR("Truncate", RoundingMode.FLOOR),
        HALF_UP("Half Away from Zero", RoundingMode.HALF_UP),
        UP("Away from Zero", RoundingMode.UP),
        DOWN("Toward Zero", RoundingMode.DOWN),
        CEILING("Ceil", RoundingMode.CEILING),
        HALF_DOWN("Half Toward Zero", RoundingMode.HALF_DOWN),
        HALF_EVEN("Half Toward Even", RoundingMode.HALF_EVEN);

        @Getter private final String name;
        private final RoundingMode mode;
    }

    @Data
    private static class Arguments {
        private String expression;
        private String comment;
        private boolean visible;
        private RoundingOption roundingMode;
        private int scale;
    }
}
