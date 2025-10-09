package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.ParameterConverter;
import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

import java.util.function.Function;

public interface DiscordArgumentParser<OUT>
        extends ParameterConverter<ChatInputInteractionEvent, OUT>,
        Visitor<ImmutableApplicationCommandRequest.Builder> {

    default <OUT2> DiscordArgumentParser<OUT2> map(Function<OUT, OUT2> mapper) {
        return new DiscordArgumentParserMapper<>(this, mapper);
    }

    record DiscordArgumentParserMapper<T, U>(DiscordArgumentParser<T> base,
                                             Function<T, U> mapper) implements DiscordArgumentParser<U> {

        @Override
        public U convert(ChatInputInteractionEvent chatInputInteractionEvent) {
            return mapper.apply(base.convert(chatInputInteractionEvent));
        }

        @Override
        public ImmutableApplicationCommandRequest.Builder visit(ImmutableApplicationCommandRequest.Builder input) {
            return base.visit(input);
        }
    }
}
