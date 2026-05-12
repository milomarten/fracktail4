package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Role;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public class RoleArgumentParser extends BaseOptionalArgumentParser<Snowflake> {
    public RoleArgumentParser(String name, String description) {
        super(name, description);
    }

    @Override
    protected ApplicationCommandOption.Type type() {
        return ApplicationCommandOption.Type.ROLE;
    }

    @Override
    public Optional<Snowflake> get(ChatInputInteractionEvent event) {
        return event.getOptionAsSnowflake(name);
    }

    @Override
    public Optional<Snowflake> get(ChatInputInteractionEvent event, ApplicationCommandInteractionOption branch) {
        return branch.getOption(name)
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asSnowflake);
    }

    /**
     * Convert this parser to returning an asynchronously-obtained Role
     * <br>
     * The provided snowflake, if present, is asynchronously resolved into the full details
     * of the role.
     * When this is invoked, `this` is effectively lost, and should no longer be used. Because
     * of this, the `required` parameter is provided to allow you to customize if the role
     * is required or not.
     * As such,
     * @param required True, if the parameter is required.
     * @return An argument which provides an asynchronous Role
     */
    public Argument<Mono<Role>> asRole(boolean required) {
        return new ResolvedRoleArgumentParser(this, required);
    }

    private record ResolvedRoleArgumentParser(RoleArgumentParser base) implements Argument<Mono<Role>> {
        public ResolvedRoleArgumentParser(RoleArgumentParser base, boolean required) {
            this(base);
            this.base.addVisitor(DiscordVisitor.argRequired(required));
        }

        @Override
        public List<ApplicationCommandOptionData> getOptions() {
            return base.getOptions();
        }

        @Override
        public Mono<Role> get(ChatInputInteractionEvent event) {
            return event.getOptionAsRole(base.name);
        }

        @Override
        public Mono<Role> get(ChatInputInteractionEvent event, ApplicationCommandInteractionOption branch) {
            return branch.getOption(base.name)
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .map(ApplicationCommandInteractionOptionValue::asRole)
                    .orElseGet(Mono::empty);
        }
    }
}
