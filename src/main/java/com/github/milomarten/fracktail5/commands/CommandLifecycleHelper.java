package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.discord.DiscordHookSource;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommandRegistry;
import com.github.milomarten.fracktail5.platform.discord.DiscordUserCommandRegistry;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommandLifecycleHelper implements DiscordHookSource {
    private final DiscordSlashCommandRegistry slashCommandRegistry;
    private final DiscordUserCommandRegistry userCommandRegistry;
    private GatewayDiscordClient client;

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        this.client = client;
        client.on(MessageCreateEvent.class, mce -> {
            if (mce.getGuildId().isPresent()) {
                return Mono.empty();
            }
            var content = mce.getMessage().getContent();
            if (content.startsWith("!")) {
                var args = StringUtils.split(content.substring(1), ' ');
                if (args.length > 0) {
                    return handleCommand(args[0], ArrayUtils.subarray(args, 1, args.length))
                            .flatMap(msg -> {
                                return mce.getMessage().getChannel()
                                        .flatMap(mc -> mc.createMessage(msg));
                            });
                }
            }
            return Mono.empty();
        }).subscribe();
    }

    private Mono<String> handleCommand(String command, String[] args) {
        if ("local".equals(command)) {
            if (args.length == 0) {
                return Mono.just("Must provide ID of server");
            }
            try {
                var snowflake = Long.parseLong(args[0]);
                return handleLocalCommand(snowflake);
            } catch (NumberFormatException ex) {
                return Mono.just("Must provide ID of server");
            }
        } else if ("global".equals(command)) {
            return handleGlobalCommand();
        }
        return Mono.empty();
    }

    private Mono<String> handleLocalCommand(long where) {
        return getApplicationId()
                .flatMap(appId -> {
                    var slashCommandSpecs = slashCommandRegistry.getSpecs();
                    var userCommandSpecs = userCommandRegistry.getSpecs();
                    return client.getRestClient()
                            .getApplicationService()
                            .bulkOverwriteGuildApplicationCommand(appId, where, concat(slashCommandSpecs, userCommandSpecs))
                            .collectList();
                })
                .map(done -> {
                    return String.format("%d command pushed to %d", done.size(), where);
                })
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    private Mono<String> handleGlobalCommand() {
        return getApplicationId()
                .flatMap(appId -> {
                    var slashCommandSpecs = slashCommandRegistry.getSpecs();
                    var userCommandSpecs = userCommandRegistry.getSpecs();
                    return client.getRestClient()
                            .getApplicationService()
                            .bulkOverwriteGlobalApplicationCommand(appId, concat(slashCommandSpecs, userCommandSpecs))
                            .collectList();
                })
                .map(done -> {
                    return String.format("%d command pushed to prod. It'll take a bit of time!", done.size());
                })
                .onErrorResume(e -> Mono.just(e.getMessage()));
    }

    private Mono<Long> getApplicationId() {
        return client.getRestClient()
                .getApplicationId();
    }

    private static <T> List<T> concat(List<T> one, List<T> two) {
        var copy = new ArrayList<>(one);
        copy.addAll(two);
        return copy;
    }
}
