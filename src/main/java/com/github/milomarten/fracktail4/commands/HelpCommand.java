package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class HelpCommand extends NormalCommand implements CommandRegistryAware {
    private CommandRegistry registry = null;

    @Override
    public CommandData getCommandData() {
        return CommandData.builder()
                .id("help")
                .alias("help")
                .description("Give help with how to use a command")
                .param(CommandData.Param.builder()
                        .name("command")
                        .optional(true)
                        .build())
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
        String prefix = registry.getConfiguration().getPrefix();
        return String.format("%s%s %s - %s",
                prefix, String.join(",", cd.getAliases()),
                helpStringForParams(cmd.getCommandData().getParams()),
                cd.getDescription());
    }

    private String helpStringForParams(List<CommandData.Param> params) {
        return params.stream()
                .map(p -> {
                    if (p.isOptional()) {
                        return "[_" + p.getName() + "_]";
                    } else {
                        return "_" + p.getName() + "_";
                    }
                })
                .collect(Collectors.joining(" "));
    }

    @Override
    public void setCommandRegistry(CommandRegistry registry) {
        this.registry = registry;
    }
}
