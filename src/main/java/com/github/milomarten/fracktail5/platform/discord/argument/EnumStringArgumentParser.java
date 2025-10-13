package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A parameter parser which supports enums by name.
 * Discord does not directly support enums, but allows for any given argument to have a list
 * of permissible values. This parser leverages this behavior to effectively handle enums.
 * <br>
 * Discord allows the value of the argument to be different from the label the front-end user
 * sees. As such, this Parser has a `namer` field, which converts the Enum name into a
 * human-friendly string.
 * <br>
 * This class is named EnumString because it uses a String parameter on the Discord side, thus
 * matching enums by name. A second option is EnumInteger, which matches enums by ordinal instead.
 * EnumInteger allows you to rename enums without needing to re-spec, while EnumString allows you
 * to rearrange enums in your codebase without needing to re-spec, so choose your favorite. EnumInteger
 * may also be slightly more efficient, but it's probably negligible.
 * @param <E> The enum type
 */
public class EnumStringArgumentParser<E extends Enum<E>> extends BaseOptionalArgumentParser<E> {
    private final Class<E> enumClass;
    private final Function<E, String> namer;

    public EnumStringArgumentParser(String name, String description, Class<E> clazz, Function<E, String> namer) {
        super(name, description);
        this.enumClass = clazz;
        this.namer = namer;

        this.addVisitor(builder -> builder.addAllChoices(constructChoices()));
    }

    private List<ApplicationCommandOptionChoiceData> constructChoices() {
        return Arrays.stream(this.enumClass.getEnumConstants())
                .map(e -> {
                    return (ApplicationCommandOptionChoiceData) (ApplicationCommandOptionChoiceData.builder()
                            .name(namer.apply(e))
                            .value(e.name())
                            .build());
                })
                .toList();
    }

    @Override
    public Optional<E> get(ChatInputInteractionEvent event) {
        return event.getOption(this.name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .map(i -> {
                    return EnumUtils.getEnum(this.enumClass, i, null);
                });
    }

    @Override
    public Optional<E> get(ChatInputInteractionEvent event, ApplicationCommandInteractionOption branch) {
        return branch.getOption(this.name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString)
                .map(i -> {
                    return EnumUtils.getEnum(this.enumClass, i, null);
                });
    }

    @Override
    protected ApplicationCommandOption.Type type() {
        return ApplicationCommandOption.Type.STRING;
    }
}
