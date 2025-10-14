package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail.core.discord.DiscordHookSource;
import com.github.milomarten.fracktail5.platform.Visitor;
import com.github.milomarten.fracktail5.platform.VisitorGroup;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The orchestrator for handling all Slash Command usage
 * Any beans implementing DiscordSlashCommand will be processed and registered for use.
 * When a slash command is on the bot, it will find the relevant command and execute it.
 * If there is no such command, a message will inform the user that the command doesn't exist.
 * <br>
 * Any beans implementing the Visitor&t;DiscordSlashCommand&gt; interface will be invoked with
 * every command that is registered, to allow for cross-cutting command code.
 */
@Component
@Slf4j
public class DiscordSlashCommandRegistry implements DiscordHookSource {
    private final Map<String, DiscordSlashCommand.Details> slashCommands;

    public DiscordSlashCommandRegistry(
            List<DiscordSlashCommand> slashCommands,
            List<Visitor<DiscordSlashCommand>> visitors
    ) {
        visitors.forEach(visitor -> {
            log.info("Registering DiscordSlashCommand visitor {}", visitor.getClass().getSimpleName());
        });
        var visitorGroup = new VisitorGroup<>(visitors);
        this.slashCommands = slashCommands.stream()
                .map(visitorGroup::visit)
                .map(DiscordSlashCommand::getDiscordSlashCommandDetails)
                .peek(cmd -> log.info("Registered Slash Command {}", cmd.getName()))
                .collect(Collectors.toMap(
                        DiscordSlashCommand.Details::getName,
                        Function.identity()
                ));
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(ChatInputInteractionEvent.class, evt -> {
           var command = slashCommands.get(evt.getCommandName());
           if (command == null) {
               log.warn("Unable to find command {}. Skipping.", evt.getCommandName());
               return evt.reply("Command has been sunset or is being rebuilt. Sorry!")
                       .withEphemeral(true);
           } else {
               var response = command.invoke(evt);
               return response.respondTo(evt)
                       .then()
                       .onErrorResume(ex -> {
                           if (ex instanceof DiscordResponse responseErr) {
                               return responseErr.respondTo(evt).then();
                           } else {
                               return Mono.error(ex);
                           }
                       });
           }
        })
        .onErrorContinue((ex, obj) -> {
            log.error("Error executing slash command {}", obj, ex);
        })
        .subscribe();
    }

    public List<ApplicationCommandRequest> getSpecs() {
        return slashCommands.values()
                .stream()
                .map(dsc -> {
                    var spec = dsc.getSpec();
                    return (ApplicationCommandRequest) (spec.visit(ApplicationCommandRequest.builder()).build());
                })
                .toList();
    }
}
