package com.github.milomarten.fracktail4.commands.dice;

import com.github.milomarten.fracktail.core.dice.DiceEvaluatorOptions;
import com.github.milomarten.fracktail.core.dice.StringDiceExpressionEvaluator;
import com.github.milomarten.fracktail.core.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.ParameterChoice;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.ParameterChoices;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
                .description("Roll some dice! See https://milomarten.github.io/fracktail4/dice for more.");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, Parameters parameters) {
        try {
            var roundingMode = Objects.requireNonNullElse(parameters.roundingmode, RoundingMode.DOWN);
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

            return Responses.reply(str, !parameters.visible);
        } catch (ExpressionSyntaxError ex) {
            return Responses.replyEphemeral(ex.getMessage());
        }
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
        private boolean visible = true;
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
        @Parameter(description = "The number of decimal digits in the output")
        @Min(0)
        @Max(9)
        private int scale = 0;
    }
}
