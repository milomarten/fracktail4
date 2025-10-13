package com.github.milomarten.fracktail5.platform.discord.argument;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;
import java.util.function.Function;

public interface Argument<TYPE> {
    List<ApplicationCommandOptionData> getOptions();
    TYPE get(ChatInputInteractionEvent event);
    TYPE get(ChatInputInteractionEvent event, ApplicationCommandInteractionOption branch);

    default <T2> Argument<T2> map(Function<TYPE, T2> func) {
        var self = this;
        return new Argument<>() {
            @Override
            public List<ApplicationCommandOptionData> getOptions() {
                return self.getOptions();
            }

            @Override
            public T2 get(ChatInputInteractionEvent event) {
                return func.apply(self.get(event));
            }

            @Override
            public T2 get(ChatInputInteractionEvent event, ApplicationCommandInteractionOption branch) {
                return func.apply(self.get(event, branch));
            }
        };
    }
}
