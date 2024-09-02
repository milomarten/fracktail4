package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandFilter;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandFilterChain;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@Slf4j
public class BotDisableCommand implements SlashCommandWrapper, SlashCommandFilter {
    private boolean lock;
    private final Map<String, Boolean> locks = new HashMap<>();

    @Override
    public Mono<Boolean> filter(ChatInputInteractionEvent event, SlashCommandFilterChain next) {
        String name = event.getCommandName();
        if ("lock".equals(name)) { return Mono.just(true); } // You cannot lock lock.
        if (lock || locks.getOrDefault(name, false)) {
            return Mono.just(false);
        }
        return next.callNext(event);
    }

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("lock")
                .description("Lock or unlock the bot, making it non-responsive to commands")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("status")
                        .description("On or Off, depending on if you want to lock or unlock the bot")
                        .type(ApplicationCommandOption.Type.BOOLEAN.getValue())
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("The command to turn off, if only one command should be disabled.")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(false)
                        .build()
                )
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var status = event.getOption("status")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asBoolean)
                .orElse(true);
        var command = event.getOption("command")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .map(ApplicationCommandInteractionOptionValue::asString);

        if (command.isEmpty()) {
            this.lock = status;
            log.info("Bot is now {}", status ? "locked" : "unlocked");
            return SlashCommands.replyEphemeral(event, "Bot status has been set to " + (status ? "locked" : "unlocked"));
        } else {
            this.locks.put(command.get(), status);
            log.info("Command {} is now {}", command.get(), status ? "locked" : "unlocked");
            return SlashCommands.replyEphemeral(event, command.get() + " status has been set to " + (status ? "locked" : "unlocked"));
        }
    }
}
