package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
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
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;

@Data
@Slf4j
@Component
public class CommandRegistry implements DiscordHookSource {
    private final CommandConfiguration configuration;
    private Map<String, Command> commands;
    private Map<String, Command> commandsByAlias;

    public CommandRegistry(List<CommandBundle> bundles, List<Command> commands, CommandConfiguration configuration) {
        this.commands = new HashMap<>();
        this.commandsByAlias = new HashMap<>();
        this.configuration = configuration;

        bundles.stream()
            .flatMap(bundle -> bundle.getCommands().stream())
            .forEach(this::registerCommand);
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
                    var match = getMatchedCommand(mce.getMessage());
                    if (match != null && match.getT1() instanceof DiscordCommand dc) {
                        Parameters params = match.getT1()
                                .getParameterParser()
                                .parse(this.configuration, match.getT2());
                        return dc.doCommand(params, mce);
                    } else {
                        return Mono.empty();
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Encountered uncaught error", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private boolean isRealPerson(Message message) {
        return message.getAuthor()
                .map(u -> !u.isBot())
                .orElse(false);
    }

    private Tuple2<Command, String> getMatchedCommand(Message message) {
        var tokens = message.getContent().split(configuration.getDelimiter(), 2);
        if (tokens[0].startsWith(configuration.getPrefix())) {
            var commandString = tokens[0].substring(1);
            return Tuples.of(this.commandsByAlias.get(commandString), tokens.length == 1 ? "" : tokens[1]);
        }
        return null;
    }
}
