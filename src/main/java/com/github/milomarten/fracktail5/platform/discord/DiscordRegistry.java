package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail5.platform.Visitor;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DiscordRegistry implements DiscordHookSource {
    private final Map<String, DiscordSlashCommand> slashCommands;

    public DiscordRegistry(
            List<DiscordSlashCommand> slashCommands,
            List<Visitor<DiscordSlashCommand>> visitors
    ) {
        visitors.forEach(visitor -> {
            log.info("Registering DiscordSlashCommand visitor {}", visitor.getClass().getSimpleName());
        });
        this.slashCommands = slashCommands.stream()
                .peek(cmd -> log.info("Registered Slash Command {}", cmd.getDiscordSlashCommandDetails().getName()))
                .map(dsc -> {
                    var wrappedDsc = dsc;
                    for (var visitor : visitors) {
                        wrappedDsc = visitor.visit(wrappedDsc);
                    }
                    return wrappedDsc;
                })
                .collect(Collectors.<DiscordSlashCommand, String, DiscordSlashCommand>toMap(
                        d -> d.getDiscordSlashCommandDetails().getName(),
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
               var config = command.getDiscordSlashCommandDetails();
               var response = config.invoke(evt);
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
        .onErrorResume(ex -> {
            log.error("Error executing command", ex);
            return Mono.empty();
        })
        .subscribe();
    }
}
