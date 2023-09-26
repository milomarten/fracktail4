package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import com.github.milomarten.fracktail4.permissions.Role;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor
public class HelpCommand implements DiscordCommand, CommandRegistryAware {
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
                .role(Role.BLOCKED) // Anyone can use the Help command, even in DMs
                .build();
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, MessageCreateEvent event) {
        return Mono.fromSupplier(() -> doHelp(parameters, event))
                .flatMap(response -> respondWith(event, response));
    }

    protected String doHelp(Parameters params, MessageCreateEvent event) {
        var subcommand = params.getParameter(0);
        if (subcommand.isPresent()) {
            var p_command = event.getMember()
                    .flatMap(member -> registry.lookupByAliasAndRole(subcommand.get(), member))
                    .or(() -> event.getMessage().getAuthor().flatMap(user -> registry.lookupByAliasAndRole(subcommand.get(), user)));
            if (p_command.isPresent()) {
                return helpStringForCommand(p_command.get());
            } else {
                return String.format("No command found with name %s", subcommand.get());
            }
        } else {
            var usableCommands = event.getMember()
                    .map(member -> registry.getUsableCommands(member))
                    .or(() -> event.getMessage().getAuthor().map(user -> registry.getUsableCommands(user)))
                    .orElseGet(List::of);
            if (usableCommands.isEmpty()) {
                return "You don't have access to any commands. Try joining Milo Marten's server!";
            }
            return usableCommands
                    .stream()
                    .sorted(Comparator.comparing(c -> c.getCommandData().getId()))
                    .map(this::helpStringForCommand)
                    .collect(Collectors.joining("\n",
                            "You have access to " + usableCommands.size() + " command(s):\n",
                            ""));
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
