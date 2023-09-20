package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.commands.HelpCommand;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Data
@Slf4j
@Component
public class CommandRegistry {
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

        if (command instanceof HelpCommand help) {
            help.setRegistry(this);
        }
    }

    public Optional<Command> lookupByAlias(String alias) {
        return Optional.ofNullable(this.commandsByAlias.get(alias));
    }

    public Collection<Command> getCommands() {
        return this.commands.values();
    }
}
