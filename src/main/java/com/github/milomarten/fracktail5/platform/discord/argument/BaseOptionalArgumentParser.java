package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;

import java.util.List;
import java.util.Optional;

public abstract class BaseOptionalArgumentParser<T> extends BaseArgumentParser<Optional<T>> {
    public BaseOptionalArgumentParser(String name, String description) {
        super(name, description);
    }

    /**
     * Mark this argument as required instead of optional.
     * This configures the Discord spec to require this input for the command.
     * If the command is invoked without it, a NoSuchElementException is thrown.
     * <br>
     * When this is invoked, `this` is effectively lost, and should no longer be used.
     * @return An argument with the same configuration as this, but marked as required.
     */
    public Argument<T> required() {
        return new BaseArgumentParserRequired<>(this);
    }

    /**
     * Mark this argument with a default value.
     * Intrinsically, this configures the Discord spec to not require this input
     * for the command, and defaulting to the supplied value if it is omitted.
     * <br>
     * When this is invoked, `this` is effectively lost, and should no longer be used.
     * @param value The value to default to
     * @return An argument with the same configuration as this, but defaulting to a specific value.
     */
    public Argument<T> defaultTo(T value) {
        return new BaseArgumentParserDefault<>(this, value);
    }

    // to do - defaultToSupplier, defaultToFunction

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
        public T get(ChatInputInteractionEvent source, ApplicationCommandInteractionOption event) {
            return base.get(source, event)
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
        public T get(ChatInputInteractionEvent source, ApplicationCommandInteractionOption event) {
            return base.get(source, event)
                    .orElse(defaultValue);
        }

        @Override
        public List<ApplicationCommandOptionData> getOptions() {
            return base.getOptions();
        }
    }
}
