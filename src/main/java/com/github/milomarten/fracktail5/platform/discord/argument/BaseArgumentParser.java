package com.github.milomarten.fracktail5.platform.discord.argument;

import com.github.milomarten.fracktail5.platform.Visitor;
import com.github.milomarten.fracktail5.platform.VisitorGroup;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordVisitor;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandOptionData;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;

import java.util.List;

public abstract class BaseArgumentParser<T>
        implements Argument<T>, Visitor<ImmutableApplicationCommandRequest.Builder> {
    protected final String name;
    private final VisitorGroup<ImmutableApplicationCommandOptionData.Builder> visitors;

    protected BaseArgumentParser(String name, String description) {
        this.name = name;
        this.visitors = new VisitorGroup<ImmutableApplicationCommandOptionData.Builder>()
                .add(DiscordVisitor.argName(name))
                .add(DiscordVisitor.argDescription(description))
                .add(DiscordVisitor.argType(type()));
    }

    protected abstract ApplicationCommandOption.Type type();

    protected void addVisitor(Visitor<ImmutableApplicationCommandOptionData.Builder> visitor) {
        this.visitors.add(visitor);
    }

    @Override
    public List<ApplicationCommandOptionData> getOptions() {
        return List.of(visitors.visit(ApplicationCommandOptionData.builder()).build());
    }

    @Override
    public ImmutableApplicationCommandRequest.Builder visit(ImmutableApplicationCommandRequest.Builder input) {
        var option = visitors.visit(ApplicationCommandOptionData.builder());
        return input.addOption(option.build());
    }
}
