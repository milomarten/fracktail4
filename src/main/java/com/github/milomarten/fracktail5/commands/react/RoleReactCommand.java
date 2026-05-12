package com.github.milomarten.fracktail5.commands.react;

import com.github.milomarten.fracktail.core.discord.react.ReactMessage;
import com.github.milomarten.fracktail.core.discord.react.RoleHandler;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class RoleReactCommand implements DiscordSlashCommand {
    @Getter private final RoleHandler reacts;

    private final ReentrantLock lock = new ReentrantLock();
    private ReactMessage<Snowflake> oven = null;

    @Override
    public Details getDiscordSlashCommandDetails() {
        // options:
        // view
        return new DiscordSubCommand("role-react", "Adjust a Role React")
                .addBranch(CreateBranch.details(this))
                .addBranch(ViewBranch.details(this))
                .addBranch(EditBranch.details(this))
                .addBranch(DeleteBranch.details(this))
                .addBranch(AddOptionBranch.details(this))
                .addBranch(RemoveOptionBranch.details(this))
                .addBranch(PublishBranch.details(this))
                .addBranch(CancelBranch.details(this))
        ;
    }

    public DiscordResponse createContents(Supplier<ReactMessage<Snowflake>> creator, DiscordResponse success) {
        var gotIt = lock.tryLock();
        DiscordResponse response;
        if (gotIt) {
            if (oven == null) {
                oven = creator.get();
                response = success;
            } else {
                response = DiscordResponses.replyEphemeral("Can't create a role-react until the previous one is published or discarded");
            }
            lock.unlock();
            return response;
        } else {
            return DiscordResponses.replyEphemeral("Concurrent edits are taking place, please wait a moment.");
        }
    }

    public DiscordResponse updateContents(Function<ReactMessage<Snowflake>, DiscordResponse> editor) {
        var gotIt = lock.tryLock();
        DiscordResponse response;
        if (gotIt) {
            if (oven != null) {
                response = editor.apply(oven);
            } else {
                response = DiscordResponses.replyEphemeral("Start by creating or editing a role-react!");
            }
            lock.unlock();
            return response;
        } else {
            return DiscordResponses.replyEphemeral("Concurrent edits are taking place, please wait a moment.");
        }
    }

    public DiscordResponse removeContents(Function<ReactMessage<Snowflake>, DiscordResponse> deleter) {
        var gotIt = lock.tryLock();
        DiscordResponse response;
        if (gotIt) {
            if (oven != null) {
                response = deleter.apply(oven);
                oven = null;
            } else {
                response = DiscordResponses.replyEphemeral("Start by creating or editing a role-react!");
            }
            lock.unlock();
            return response;
        } else {
            return DiscordResponses.replyEphemeral("Concurrent edits are taking place, please wait a moment.");
        }
    }
}
