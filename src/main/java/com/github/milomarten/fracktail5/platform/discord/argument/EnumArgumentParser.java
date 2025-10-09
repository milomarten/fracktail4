package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.apache.commons.lang3.EnumUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class EnumArgumentParser<E extends Enum<E>> extends BaseOptionalArgumentParser<E> {
    private final Class<E> enumClass;
    private final Function<E, String> namer;

    public EnumArgumentParser(String name, String description, Class<E> clazz, Function<E, String> namer) {
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
}
