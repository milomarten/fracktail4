package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.ParameterChoice;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.ParameterChoices;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class DiceSlashCommand extends AbstractSlashCommand<DiceSlashCommand.Parameters> {
    private final StringDiceExpressionEvaluator evaluator;

    @Override
    public Class<Parameters> getParameterClass() {
        return Parameters.class;
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder) {
        return builder
                .name("roll")
                .description("Roll some dice! Use `syntax` as the expression for details.");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, Parameters parameters) {
        if ("syntax".equalsIgnoreCase(parameters.expression)) {
            return Responses.replyEphemeral(SYNTAX);
        }

        try {
            var roundingMode = Objects.requireNonNullElse(parameters.roundingmode, RoundingMode.DOWN);
            var result = evaluator.evaluate(parameters.expression, DiceEvaluatorOptions.builder()
                    .roundingMode(roundingMode)
                    .build());
            String str;
            if (result.value() == null) {
                str = String.format("%s", result.representation());
            } else {
                str = String.format("%s = **%s**", result.representation(), result.valueAsInt(roundingMode));
            }

            if (StringUtils.isNotBlank(parameters.comment)) {
                str = parameters.comment + "\n" + str;
            }

            return Responses.reply(str, !parameters.visible);
        } catch (ExpressionSyntaxError ex) {
            return Responses.replyEphemeral(ex.getMessage());
        }
    }

    private static final String SYNTAX = """
            Expressions are written as you would write a normal math equation. \
            As such, you can use normal numbers, positive, negative, +, -, \\*, and /. However, in addition to numbers, you can \
            also write *dice expressions*. The format for a dice roll is `<number of dice>d<faces on the dice>`. For example, \
            2d10 would roll 2 ten-sided dice, and add the results.
            
            Note that the case of the `d` matters. `d` will perform a normal dice roll, while `D` will perform a \
            *dotted* dice roll. This is a shorthand that will, by default:
            - Explode "infinitely" on the highest dice value
            - Switch to Success Counting mode, where a success is 7 or more, and a failure is 1.
            - Additionally, each highest dice value counts as 2 successes instead of 1.
            The explode value, success value, and failure value can all still be changed as described below.

            There is additional syntax that can augment a dice roll. All of these values can be supplied by further math expressions, \
            but all will be coerced to a whole number by dropping everything after the decimal point.:
            - x#: Drop # of the lowest dice.
            - k#: Keep the highest # dice, and discard the rest. K is shorthand for k1.
            - l#: Keep the *lowest* # dice, and discard the rest. L is shorthand for l1.
            - r#: Reroll any dice less than #. To keep rerolling, use R instead.
            - e#: Roll 1 new dice per roll greater than #. To keep exploding, use E instead.
            - s#: Switch to Success Counting mode. Instead of adding the face value of the dice, \
            the bot will add the number of dice greater than or equal to #.
            - f#: Switch to Success Counting mode. Instead of adding the face value of the dice, \
            the bot will subtract the number of dice less than or equal to #.
            
            In addition to the standard math operators, there are these operators as well:
            - #^: Round the number, rounding upward.
            - ,: Create a dice pool.
            """;

    static {
        if (SYNTAX.length() > 2000) { }
    }

    @Data
    public static class Parameters {
        @NotNull
        @Parameter(description = "The roll expression to evaluate.")
        @NotBlank(message = "Expression must have a value")
        private String expression;
        @Parameter(description = "A small description of the roll")
        private String comment;
        @Parameter(description = "Whether this role should be visible to all")
        private boolean visible;
        @Parameter(description = "The rounding function for your system", type = ApplicationCommandOption.Type.STRING)
        @ParameterChoices(choices = {
                @ParameterChoice(name = "Truncate", value = "FLOOR"),
                @ParameterChoice(name = "Half Away from Zero", value = "HALF_UP"),
                @ParameterChoice(name = "Away from Zero", value = "UP"),
                @ParameterChoice(name = "Toward Zero", value = "DOWN"),
                @ParameterChoice(name = "Ceil", value = "CEILING"),
                @ParameterChoice(name = "Half Toward Zero", value = "HALF_DOWN"),
                @ParameterChoice(name = "Half Toward Even", value = "HALF_EVEN")
        })
        private RoundingMode roundingmode;
    }
}
