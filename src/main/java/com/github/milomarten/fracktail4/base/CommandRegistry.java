package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import com.github.milomarten.fracktail4.hook.DiscordHookSource;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Data
@Slf4j
@Component
public class CommandRegistry implements DiscordHookSource {
    @Value("${command.prefix:!}")
    @Getter @Setter
    protected String prefix;

    @Value("${command.delimiter: }")
    @Getter @Setter
    protected String delimiter;

    private Map<String, Command> commands;
    private Map<String, Command> commandsByAlias;

    public CommandRegistry(List<Command> commands) {
        this.commands = new HashMap<>();
        this.commandsByAlias = new HashMap<>();
        commands.forEach(this::registerCommand);
    }

    public void registerCommand(Command command) {
        CommandData cd = command.getCommandData();
        var old = this.commands.put(cd.getId(), command);
        log.info("Registering command {} for type {}", cd.getId(), command.getClass().getSimpleName());
        if (old != null) {
            log.warn("Encountered two commands with shared ID {}. Overwriting {} with {}", cd.getId(), old, command);
        }
        for (String alias : cd.getAliases()) {
            var oldAlias = this.commandsByAlias.put(alias, command);
            if (oldAlias != null) {
                log.warn("Encountered two commands with shared alias {}. Overwriting command {} with {}",
                        alias, oldAlias.getCommandData().getId(), cd.getId());
            }
        }

        if (command instanceof CommandRegistryAware aware) {
            aware.setCommandRegistry(this);
        }
    }

    public Optional<Command> lookupByAlias(String alias) {
        return Optional.ofNullable(this.commandsByAlias.get(alias));
    }

    public Collection<Command> getCommands() {
        return this.commands.values();
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(mce -> isRealPerson(mce.getMessage()))
                .flatMap(mce -> {
                    Command match = getMatchedCommand(mce.getMessage());
                    if (match instanceof DiscordCommand dc) {
                        return dc.doCommand(mce);
                    } else {
                        return Mono.empty();
                    }
                })
                .subscribe();
    }

    private boolean isRealPerson(Message message) {
        return message.getAuthor()
                .map(u -> !u.isBot())
                .orElse(false);
    }

    private Command getMatchedCommand(Message message) {
        var tokens = message.getContent().split(delimiter, 2);
        if (tokens[0].startsWith(prefix)) {
            var commandString = tokens[0].substring(1);
            return this.commandsByAlias.get(commandString);
        }
        return null;
    }
}
