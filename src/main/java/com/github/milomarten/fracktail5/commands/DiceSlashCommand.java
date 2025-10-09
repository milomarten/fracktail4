package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail.core.dice.StringDiceExpressionEvaluator;
import com.github.milomarten.fracktail.core.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail5.platform.discord.DiscordArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.argument.BooleanArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.util.DiscordResponses;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class DiceSlashCommand implements DiscordSlashCommand {
    private final StringDiceExpressionEvaluator evaluator;

    @Override
    public Details getDiscordSlashCommandDetails() {
        return new DiscordArgumentDetails<>(
                "roll",
                "Roll some dice! See https://milomarten.github.io/fracktail4/dice for more.",
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
                ,
                this::roll
        );
    }

    private DiscordResponse roll(Arguments parameters) {
        try {
            var roundingMode = Objects.requireNonNullElse(parameters.roundingMode, RoundingMode.DOWN);
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

    @Data
    private static class Arguments {
        private String expression;
        private String comment;
        private boolean visible;
        private RoundingMode roundingMode;
        private int scale;
    }
}
