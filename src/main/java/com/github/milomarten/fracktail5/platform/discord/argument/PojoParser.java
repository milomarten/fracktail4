package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * An Argument that creates a Plain Old Java Object from the command's context.
 * The PojoParser is a constructor, plus one or more fields. When a command is used:
 * 1. The constructor is invoked
 * 2. Each field is invoked, in order of creation. The value in the event context is read,
 * possibly converted into another type, and stored using a setter.
 * A field may, itself, be another POJO. When creating the spec for discord,
 * all nested POJOs are flattened, and duplicate fields (as determined by name) are removed.
 * This allows for collecting logical groups of parameters together into one POJO,
 * and for sharing fields between them as well.
 * I don't have a solution for Subcommands written yet, but I will soon.
 * @param <ARG> The type created by this Parser
 */
public class PojoParser<ARG> implements Argument<ARG>{
    private final Supplier<ARG> constructor;
    private final List<SetterField<ARG, ?>> fields;

    /**
     * Initialize this parser
     * @param constructor The function that constructs an empty POJO
     */
    public PojoParser(Supplier<ARG> constructor) {
        this.constructor = constructor;
        this.fields = new ArrayList<>();
    }

    /**
     * Add a field mapper that pulls from the context and stores it in the POJO
     * @param argument The description of the field to pull
     * @param setter The setter to use on what comes from the context
     * @return This, for chaining
     * @param <TYPE> The type within the field
     */
    public <TYPE> PojoParser<ARG> addField(Argument<TYPE> argument, BiConsumer<ARG, TYPE> setter) {
        this.fields.add(new SetterField<>(argument, setter));
        return this;
    }

    /**
     * Add a field mapper that pulls an optional field from the context, and stores it in the POJO if present
     * Syntactic sugar for addField, where the setter is only invoked if the argument is present.
     * @param argument The description of the field to pull
     * @param setter The setter to invoke on the result of the argument
     * @return This, for chaining
     * @param <TYPE> The type within the field
     */
    public <TYPE> PojoParser<ARG> addOptionalField(Argument<Optional<TYPE>> argument, BiConsumer<ARG, TYPE> setter) {
        return addField(argument,
                (arg, opt) -> opt.ifPresent(value -> setter.accept(arg, value)));
    }

    @Override
    public ARG get(ChatInputInteractionEvent chatInputInteractionEvent) {
        var obj = constructor.get();
        for (var field : this.fields) {
            field.set(obj, chatInputInteractionEvent);
        }
        return obj;
    }

    @Override
    public List<ApplicationCommandOptionData> getOptions() {
        return fields.stream()
                .map(sf -> sf.arg)
                .map(Argument::getOptions)
                .flatMap(List::stream)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toMap(
                                    ApplicationCommandOptionData::name,
                                    Function.identity(),
                                    (one, two) -> two
                            ),
                                map -> new ArrayList<>(map.values())
                        )
                );
    }

    private record SetterField<ARG, TYPE>(Argument<TYPE> arg, BiConsumer<ARG, TYPE> setter) {
        public void set(ARG arg, ChatInputInteractionEvent chatInputInteractionEvent) {
            var param = this.arg.get(chatInputInteractionEvent);
            this.setter.accept(arg, param);
        }
    }
}
