package com.github.milomarten.fracktail4.base;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

public abstract class AbstractPrefixedCommand implements Command {
    @Value("${command.prefix:!}")
    @Getter
    protected String prefix;

    @Value("${command.delimiter: }")
    protected String delimiter;

    @Override
    public void addHook(GatewayDiscordClient client) {
        CommandData data = getCommandData();
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(mce -> matchesCommand(mce.getMessage(), data))
                .flatMap(this::doCommand)
                .subscribe();
    }

    protected abstract Mono<?> doCommand(MessageCreateEvent event);

    private boolean matchesCommand(Message message, CommandData commandData) {
        var tokens = message.getContent().split(delimiter);
        if (tokens[0].startsWith(prefix)) {
            var command = tokens[0].substring(1);
            return commandData.getAliases().contains(command);
        }
        return false;
    }
}
