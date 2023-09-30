package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.permissions.Role;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
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
                .flow(CommandFlow.builder()
                        .param(CommandParam.builder()
                                .name("command")
                                .optional(true)
                                .build())
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
                return getHelpText(p_command.get());
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
                    .map(HelpCommand::getSummaryText)
                    .collect(Collectors.joining("\n",
                            "You have access to " + usableCommands.size() + " command(s):\n",
                            ""));
        }
    }

    @Override
    public void setCommandRegistry(CommandRegistry registry) {
        this.registry = registry;
    }

    private static String getSummaryText(Command cmd) {
        CommandData cd = cmd.getCommandData();
        return String.format("%s - %s",
                String.join(",", cd.getAliases()),
                cd.getDescription()
        );
    }

    private static String getHelpText(Command cmd) {
        CommandData cd = cmd.getCommandData();
        String canon = cd.getAliases().iterator().next();

        if (cd.getFlows().isEmpty()) {
            return String.format("%s - %s",
                    String.join(",", cd.getAliases()),
                    cd.getDescription()
            );
        }
        return String.format("%s - %s\nUsage:\n%s",
                String.join(",", cd.getAliases()),
                cd.getDescription(),
                cd.getFlows().stream()
                        .map(flow -> helpStringForParams(canon, flow))
                        .collect(Collectors.joining("\n\t", "\t", "")));
    }

    private static String helpStringForParams(String alias, CommandFlow flow) {
        return flow.getParams().stream()
                .map(p -> {
                    if (p.isOptional()) {
                        return "[" + p.getName() + "]";
                    } else {
                        return p.getType().format(p.getName());
                    }
                })
                .collect(Collectors.joining(" ", "`" + alias + " ", "` - " + flow.getDescription()));
    }
}
