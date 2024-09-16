package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.commands.dice.StringDiceExpressionEvaluator;
import com.github.milomarten.fracktail4.commands.dice.Utils;
import com.github.milomarten.fracktail4.commands.dice.term.ExpressionSyntaxError;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class DiceSlashCommand implements SlashCommandWrapper {
    private final StringDiceExpressionEvaluator evaluator;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("roll")
                .description("Roll some dice! Use `syntax` as the expression for details.")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("expression")
                        .description("The roll expression to evaluate.")
                        .required(true)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("comment")
                        .description("A small description of the roll")
                        .required(false)
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .maxLength(240)
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("visible")
                        .description("Whether this role should be visible to all")
                        .required(false)
                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                        .build())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var expression = event.getOption("expression")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString())
                .orElseThrow();
        if (expression.equalsIgnoreCase("syntax")) {
            return SlashCommands.replyEphemeral(event, SYNTAX);
        }
        var commentOpt = event.getOption("comment")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString());
        var visible = event.getOption("visible")
                .flatMap(a -> a.getValue())
                .map(a -> a.asBoolean())
                .orElse(true);
        try {
            var result = evaluator.evaluate(expression);
            String str = String.format("```ansi\n%s = %s\n```", result.representation(), Utils.outputBigDecimal(result.value()));

            return event.reply(commentOpt.map(comment -> comment + "\n" + str).orElse(str))
                    .withEphemeral(!visible);
        } catch (ExpressionSyntaxError ex) {
            return SlashCommands.replyEphemeral(event, ex.getMessage());
        }
    }

    private static final String SYNTAX = """
            Expressions are written as you would write a normal math equation. \
            As such, you can use normal numbers, positive, negative, +, -, \\*, and /. However, in addition to numbers, you can \
            also write *dice expressions*. The format for a dice roll is `<number of dice>d<faces on the dice>`. For example, \
            2d10 would roll 2 ten-sided dice, and add the results.

            There is additional syntax that can augment a dice roll. All of these values can be supplied by further math expressions, \
            but all will be coerced to a whole number by dropping everything after the decimal point.:
            - x#: Drop # of the lowest dice.
            - k#: Keep the highest # dice, and discard the rest.
            - l#: Keep the *lowest* # dice, and discard the rest.
            - r#: Reroll any dice less than #. To keep rerolling, use R instead.
            - e#: Roll 1 new dice per roll greater than #. To keep exploding, use E instead.
            - s#: Switch to Success Counting mode. Instead of adding the face value of the dice, \
            the bot will add the number of dice greater than or equal to #.
            - f#: Switch to Success Counting mode. Instead of adding the face value of the dice, \
            the bot will subtract the number of dice less than or equal to #.
            
            In addition to the standard math operators, there are these operators as well:
            - #^: Round the number, rounding upward.
            - <#: Low-cap the number. If the number is less than #, # will be used instead.
            - >#: High-cap the number. If the number is greater than #, # will be used instead.
            """;
}
