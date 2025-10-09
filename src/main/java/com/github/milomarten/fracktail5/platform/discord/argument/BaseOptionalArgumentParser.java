package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

import java.util.List;
import java.util.Optional;

public abstract class BaseOptionalArgumentParser<T> extends BaseArgumentParser<Optional<T>> {
    public BaseOptionalArgumentParser(String name, String description) {
        super(name, description);
    }

    public Argument<T> required() {
        return new BaseArgumentParserRequired<>(this);
    }

    public Argument<T> defaultTo(T value) {
        return new BaseArgumentParserDefault<>(this, value);
    }

    private record BaseArgumentParserRequired<T>(BaseOptionalArgumentParser<T> base) implements Argument<T> {
        public BaseArgumentParserRequired(BaseOptionalArgumentParser<T> base) {
            this.base = base;
            this.base.addVisitor(DiscordVisitor.argRequired(true));
        }

        @Override
        public T get(ChatInputInteractionEvent chatInputInteractionEvent) {
            return base.get(chatInputInteractionEvent)
                    .orElseThrow();
        }

        @Override
        public List<ApplicationCommandOptionData> getOptions() {
            return base.getOptions();
        }
    }

    private record BaseArgumentParserDefault<T>(BaseOptionalArgumentParser<T> base,
                                                T defaultValue) implements Argument<T> {
        public BaseArgumentParserDefault(BaseOptionalArgumentParser<T> base, T defaultValue) {
            this.base = base;
            this.defaultValue = defaultValue;
            this.base.addVisitor(DiscordVisitor.argRequired(false));
        }

        @Override
        public T get(ChatInputInteractionEvent chatInputInteractionEvent) {
            return base.get(chatInputInteractionEvent)
                    .orElse(defaultValue);
        }

        @Override
        public List<ApplicationCommandOptionData> getOptions() {
            return base.getOptions();
        }
    }
}
