package com.github.milomarten.fracktail5.platform.discord;

import com.github.milomarten.fracktail.core.discord.DiscordHookSource;
import com.github.milomarten.fracktail5.platform.Visitor;
import com.github.milomarten.fracktail5.platform.VisitorGroup;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DiscordUserCommandRegistry implements DiscordHookSource {
    private final Map<String, DiscordUserCommand.Details> discordUserCommands;

    public DiscordUserCommandRegistry(
            List<DiscordUserCommand> userCommands,
            List<Visitor<DiscordUserCommand>> visitors
    ) {
        visitors.forEach(visitor -> {
            log.info("Registering DiscordSlashCommand visitor {}", visitor.getClass().getSimpleName());
        });
        var visitorGroup = new VisitorGroup<>(visitors);
        this.discordUserCommands = userCommands.stream()
                .map(visitorGroup::visit)
                .map(DiscordUserCommand::getDiscordUserCommandDetails)
                .peek(cmd -> log.info("Registered User Command {}", cmd.getName()))
                .collect(Collectors.toMap(
                        DiscordUserCommand.Details::getName,
                        Function.identity()
                ));
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(UserInteractionEvent.class, evt -> {
           var command = discordUserCommands.get(evt.getCommandName());
           if (command == null) {
               log.warn("Unable to find command {}. Skipping.", evt.getCommandName());
               return evt.reply("Command has been sunset or is being rebuilt. Sorry!")
                       .withEphemeral(true);
           } else {
               var response = command.invoke(evt);
               return response.respondTo(evt)
                       .then()
                       .onErrorResume(ex -> {
                           if (ex instanceof DiscordUserResponse responseErr) {
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

    public List<ApplicationCommandRequest> getSpecs() {
        return discordUserCommands.values()
                .stream()
                .map(dsc -> {
                    return (ApplicationCommandRequest) (ApplicationCommandRequest.builder()
                            .name(dsc.getName())
                            .type(2)
                            .build());
                })
                .toList();
    }
}
