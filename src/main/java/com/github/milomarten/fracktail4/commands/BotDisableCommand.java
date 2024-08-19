package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.base.filter.CommandFilter;
import com.github.milomarten.fracktail4.base.filter.CommandFilterChain;
import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.permissions.Role;
import com.github.milomarten.fracktail4.slash.SlashCommandFilter;
import com.github.milomarten.fracktail4.slash.SlashCommandFilterChain;
import com.github.milomarten.fracktail4.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
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
public class BotDisableCommand implements AllPlatformCommand, CommandFilter, SlashCommandWrapper, SlashCommandFilter {
    private static final Set<String> PERMISSIBLE_ARG1_VALUES = Set.of("on", "off");

    private boolean lock;
    private final Map<String, Boolean> locks = new HashMap<>();

    @Override
    public CommandData getCommandData() {
        return CommandData.builder()
                .id("lock")
                .alias("lock")
                .description("Lock or unlock the bot, making it non-responsive to commands")
                .flow(CommandFlow.builder()
                        .description("Lock the bot. If command name is provided, only that command is locked.")
                        .param(CommandParam.builder().name("on").build())
                        .param(CommandParam.builder().name("command").optional(true).build())
                        .build())
                .flow(CommandFlow.builder()
                        .description("Unlock the bot. If command name is provided, only that command is unlocked.")
                        .param(CommandParam.builder().name("off").build())
                        .param(CommandParam.builder().name("command").optional(true).build())
                        .build())
                .build();
    }

    @Override
    public Role getRequiredRole() {
        return Role.ADMIN;
    }

    @Override
    public Mono<Boolean> filter(Command command, Parameters parameters, Object event, CommandFilterChain next) {
        // Exclude the command itself, else we can't unlock!
        // Global lock supersedes individual locks
        if (command != this && (this.lock || this.locks.getOrDefault(command.getCommandData().getId(), false))) {
            return Mono.just(false);
        }
        return next.callNext(command, parameters, event);
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, Context context) {
        Optional<String> lockUnlock = parameters.getParameter(0);
        Optional<String> cmdToUse = parameters.getParameter(1);

        if (lockUnlock.isEmpty() || !PERMISSIBLE_ARG1_VALUES.contains(lockUnlock.get())) {
            return context.respond("Correct usage: `!lock _on|off_ <command>`");
        }

        var lockOrNot = lockUnlock.get().equals("on");

        if (cmdToUse.isEmpty()) {
            this.lock = lockOrNot;
            return context.respond("Bot has been " + (lockOrNot ? "locked" : "unlocked"));
        } else {
            this.locks.put(cmdToUse.get(), lockOrNot);
            return context.respond("Command " + cmdToUse.get() + " has been " + (lockOrNot ? "locked" : "unlocked"));
        }
    }

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
            return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .content("Bot status has been set to " + (status ? "locked" : "unlocked"))
                    .ephemeral(true)
                    .build());
        } else {
            this.locks.put(command.get(), status);
            log.info("Command {} is now {}", command.get(), status ? "locked" : "unlocked");
            return event.reply(InteractionApplicationCommandCallbackSpec.builder()
                    .content(command.get() + " status has been set to " + (status ? "locked" : "unlocked"))
                    .ephemeral(true)
                    .build());
        }
    }
}
