package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

import java.util.Optional;

public abstract class BaseOptionalArgumentParser<T> extends BaseArgumentParser<Optional<T>> {
    public BaseOptionalArgumentParser(String name, String description) {
        super(name, description);
    }

    public DiscordArgumentParser<T> required() {
        return new BaseArgumentParserRequired<>(this);
    }

    public DiscordArgumentParser<T> defaultTo(T value) {
        return new BaseArgumentParserDefault<>(this, value);
    }

    private record BaseArgumentParserRequired<T>(BaseOptionalArgumentParser<T> base) implements DiscordArgumentParser<T> {
        public BaseArgumentParserRequired(BaseOptionalArgumentParser<T> base) {
            this.base = base;
            this.base.addVisitor(DiscordVisitor.argRequired(true));
        }

        @Override
        public T convert(ChatInputInteractionEvent chatInputInteractionEvent) {
            return base.convert(chatInputInteractionEvent)
                    .orElseThrow();
        }

        @Override
        public ImmutableApplicationCommandRequest.Builder visit(ImmutableApplicationCommandRequest.Builder input) {
            return base.visit(input);
        }
    }

    private record BaseArgumentParserDefault<T>(BaseOptionalArgumentParser<T> base,
                                                T defaultValue) implements DiscordArgumentParser<T> {
        public BaseArgumentParserDefault(BaseOptionalArgumentParser<T> base, T defaultValue) {
            this.base = base;
            this.defaultValue = defaultValue;
            this.base.addVisitor(DiscordVisitor.argRequired(false));
        }

        @Override
        public T convert(ChatInputInteractionEvent chatInputInteractionEvent) {
            return base.convert(chatInputInteractionEvent)
                    .orElse(defaultValue);
        }

        @Override
        public ImmutableApplicationCommandRequest.Builder visit(ImmutableApplicationCommandRequest.Builder input) {
            return base.visit(input);
        }
    }
}
