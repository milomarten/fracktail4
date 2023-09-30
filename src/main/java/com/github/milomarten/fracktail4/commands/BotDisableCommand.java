package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.base.filter.CommandFilter;
import com.github.milomarten.fracktail4.base.filter.CommandFilterChain;
import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.permissions.Role;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
public class BotDisableCommand implements AllPlatformCommand, CommandFilter {
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
}
