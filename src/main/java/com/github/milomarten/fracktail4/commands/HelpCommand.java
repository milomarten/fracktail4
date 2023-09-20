package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class HelpCommand extends NormalPrefixedCommand {
    @Setter private CommandRegistry registry = null;

    @Override
    public CommandData getCommandData() {
        return CommandData.builder()
                .id("help")
                .alias("help")
                .description("Give help with how to use a command")
                .build();
    }

    @Override
    protected String doCommand(Parameters params) {
        var subcommand = params.getParameter(0);
        if (subcommand.isPresent()) {
            var p_command = registry.lookupByAlias(subcommand.get());
            if (p_command.isPresent()) {
                return helpStringForCommand(p_command.get());
            } else {
                return String.format("No command found with name %s", subcommand.get());
            }
        } else {
            return registry.getCommands()
                    .stream()
                    .sorted(Comparator.comparing(c -> c.getCommandData().getId()))
                    .map(this::helpStringForCommand)
                    .collect(Collectors.joining("\n"));
        }
    }

    private String helpStringForCommand(Command cmd) {
        CommandData cd = cmd.getCommandData();
        String prefix = "";
        if (cmd instanceof AbstractPrefixedCommand prefixed) {
            prefix = prefixed.getPrefix();
        }
        return String.format("%s%s - %s", prefix, String.join(",", cd.getAliases()), cd.getDescription());
    }
}
