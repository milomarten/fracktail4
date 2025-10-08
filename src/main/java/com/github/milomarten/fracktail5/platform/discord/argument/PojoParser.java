package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class PojoParser<ARG> implements DiscordArgumentParser<ARG>, Argument<ARG>{
    private final Supplier<ARG> constructor;
    private final List<SetterField<ARG, ?>> arguments;

    public PojoParser(Supplier<ARG> constructor) {
        this.constructor = constructor;
        this.arguments = new ArrayList<>();
    }

    public <TYPE> PojoParser<ARG> addField(Argument<TYPE> argument, BiConsumer<ARG, TYPE> setter) {
        this.arguments.add(new SetterField<>(argument, setter));
        return this;
    }

    @Override
    public ARG convert(ChatInputInteractionEvent chatInputInteractionEvent) {
        var obj = constructor.get();
        arguments.forEach(argument -> argument.set(obj, chatInputInteractionEvent));
        return obj;
    }

    @Override
    public ImmutableApplicationCommandRequest.Builder visit(ImmutableApplicationCommandRequest.Builder input) {
        return input.addAllOptions(getOptions());
    }

    @Override
    public List<ApplicationCommandOptionData> getOptions() {
        return arguments.stream()
                .map(sf -> sf.arg)
                .map(Argument::getOptions)
                .flatMap(List::stream)
                .toList();
    }

    @Override
    public ARG get(ChatInputInteractionEvent event) {
        return convert(event);
    }

    private record SetterField<ARG, TYPE>(Argument<TYPE> arg, BiConsumer<ARG, TYPE> setter) {
        public void set(ARG arg, ChatInputInteractionEvent chatInputInteractionEvent) {
            var param = this.arg.get(chatInputInteractionEvent);
            this.setter.accept(arg, param);
        }
    }
}
