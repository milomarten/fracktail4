package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PojoParser<ARG> implements Argument<ARG>{
    private final Supplier<ARG> constructor;
    private final List<SetterField<ARG, ?>> fields;

    public PojoParser(Supplier<ARG> constructor) {
        this.constructor = constructor;
        this.fields = new ArrayList<>();
    }

    public <TYPE> PojoParser<ARG> addField(Argument<TYPE> argument, BiConsumer<ARG, TYPE> setter) {
        this.fields.add(new SetterField<>(argument, setter));
        return this;
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
