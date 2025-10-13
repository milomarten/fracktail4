package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail5.platform.Visitor;
import com.github.milomarten.fracktail5.platform.discord.DiscordArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.WrappedDiscordSlashCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.argument.BooleanArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class LockCommand implements DiscordSlashCommand, Visitor<DiscordSlashCommand> {
    private final Details SPEC = new DiscordArgumentDetails<>(
            "lock",
            "Lock or unlock the bot, making it non-responsive to commands",
            new PojoParser<>(Parameters::new)
            .addField(new BooleanArgumentParser(
                  "status",
                    "On or Off, depending on if you want to lock or unlock the bot"
            ).required(), Parameters::setLock)
            .addField(new StringArgumentParser(
                  "command",
                    "The command to turn off, if only one command should be disabled."
            ).defaultTo(null), Parameters::setCommand)
            .addField(new StringArgumentParser(
                    "reason",
                    "The reason for locking the command"
            ).defaultTo(null), Parameters::setReason),
            this::lock
    );

    private LockWithReason totalLock = LockWithReason.UNLOCKED;
    private final Map<String, LockWithReason> commandLock = new HashMap<>();

    @Override
    public DiscordSlashCommand visit(DiscordSlashCommand input) {
        return new WrappedDiscordSlashCommandDetails(input) {
            @Override
            public DiscordResponse invoke(ChatInputInteractionEvent event) {
                if (isLocked(event.getCommandName())) {
                    var reason = getLockReason(event.getCommandName());
                    if (reason == null) {
                        return DiscordResponses.replyEphemeral("Command is locked.");
                    } else {
                        return DiscordResponses.replyEphemeral(
                                "Command is locked: " + reason
                        );
                    }
                }
                return invokeWrapped(event);
            }
        };
    }

    @Override
    public Details getDiscordSlashCommandDetails() {
        return SPEC;
    }

    private DiscordResponse lock(Parameters input) {
        if ("lock".equals(input.getCommand())) {
            return DiscordResponses.replyEphemeral("You cannot lock the lock command!");
        }

        if (input.getCommand() == null) {
            this.totalLock = input.toLockWithReason();
            if (input.lock) {
                return DiscordResponses.replyEphemeral("All commands are locked.");
            } else {
                return DiscordResponses.replyEphemeral("All commands are unlocked.");
            }
        } else {
            this.commandLock.put(input.command, input.toLockWithReason());
            if (input.lock) {
                return DiscordResponses.replyEphemeral("Locked command " + input.command + "!");
            } else {
                return DiscordResponses.replyEphemeral("Unlocked command " + input.command + "!");
            }
        }
    }

    private boolean isLocked(String command) {
        return this.totalLock.locked
                || this.commandLock.getOrDefault(command, LockWithReason.UNLOCKED).locked;
    }

    public String getLockReason(String command) {
        if (this.totalLock.locked) {
            return this.totalLock.reason;
        } else {
            return this.commandLock.getOrDefault(command, LockWithReason.UNLOCKED).reason;
        }
    }

    @Data
    private static class Parameters {
        boolean lock;
        String command;
        String reason;

        public LockWithReason toLockWithReason() {
            return new LockWithReason(lock, reason);
        }
    }

    private record LockWithReason(boolean locked, String reason) {
        public static final LockWithReason UNLOCKED = new LockWithReason(false, null);
    }
}
