package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.permissions.Role;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class HelpCommand implements AllPlatformCommand, CommandRegistryAware {
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
    public Role getRequiredRole() {
        return Role.BLOCKED; // Anyone can use the Help command, even in DMs
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, Context context) {
        return Mono.fromSupplier(() -> doHelp(parameters, context))
                .flatMap(context::respond);
    }

    protected String doHelp(Parameters params, Context context) {
        var subcommand = params.getParameter(0);
        if (subcommand.isPresent()) {
            var p_command = registry.lookupByAliasAndRole(subcommand.get(), context.getRole());
            if (p_command.isPresent()) {
                return p_command.get().getHelpText();
            } else {
                return String.format("No command found with name %s", subcommand.get());
            }
        } else {
            var usableCommands = registry.getUsableCommands(context.getRole());
            if (usableCommands.isEmpty()) {
                return "You don't have access to any commands. Try joining Milo Marten's server!";
            }
            return usableCommands
                    .stream()
                    .sorted(Comparator.comparing(c -> c.getCommandData().getId()))
                    .map(Command::getHelpText)
                    .collect(Collectors.joining("\n",
                            "You have access to " + usableCommands.size() + " command(s):\n",
                            ""));
        }
    }

    @Override
    public void setCommandRegistry(CommandRegistry registry) {
        this.registry = registry;
    }
}
