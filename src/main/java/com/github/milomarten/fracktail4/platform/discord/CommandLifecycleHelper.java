package com.github.milomarten.fracktail4.platform.discord;

import com.github.milomarten.fracktail4.config.FracktailRoles;
import com.github.milomarten.fracktail4.permissions.PermissionsProvider;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandRegistry;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ApplicationCommandData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommandLifecycleHelper implements DiscordHookSource {
    private final PermissionsProvider<User, FracktailRoles> permissionsProvider;
    private final SlashCommandRegistry registry;

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(MessageCreateEvent.class).flatMap(mce -> {
                    var message = mce.getMessage();
                    var author = message.getAuthor();
                    if (author.isEmpty() || permissionsProvider.getRoles(author.get()).doesNotHaveRole(FracktailRoles.OWNER)) {
                        return Flux.empty();
                    }
                    String[] args = StringUtils.split(message.getContent());
                    if ("!updateGlobalCommands".equals(args[0])){
                        // Send every command to the world.
                        return getId(client)
                                .flatMapMany(id -> client.getRestClient().getApplicationService()
                                        .bulkOverwriteGlobalApplicationCommand(id, registry.getRequests())
                                )
                                .count()
                                .flatMap(count -> reply(message, "Updated " + count + " commands globally. Remember it may take a bit!"));
                    } else if ("!deleteGuildCommands".equals(args[0])) {
                        // Delete every command from the guild.
                        var guildIdMaybe = getGuildIdFromCommand(mce, args);
                        if (guildIdMaybe.isEmpty()) {
                            return reply(mce.getMessage(), "Since you're in a DM, Guild ID is required for guild commands");
                        }
                        return getId(client)
                                .flatMapMany(id -> client.getRestClient().getApplicationService()
                                        .getGuildApplicationCommands(id, guildIdMaybe.get())
                                        .map(acd -> Tuples.of(id, acd))
                                )
                                .flatMap(tuple -> client.getRestClient().getApplicationService()
                                        .deleteGuildApplicationCommand(tuple.getT1(), guildIdMaybe.get(), tuple.getT2().id().asLong())
                                        .thenReturn(1)
                                )
                                .count()
                                .flatMap(count -> reply(message, "Deleted " + count + " commands locally."));
                    } else if ("!updateGuildCommand".equals(args[0])) {
                        // Push a single command to the guild.
                        var params = getGuildAndCommandNames(mce, args);
                        if (params.isEmpty()) {
                            return reply(mce.getMessage(), "Since you're in a DM, Guild ID is required for guild commands");
                        }
                        var guildId = params.get().getT1();
                        String commandName = params.get().getT2();
                        var command = registry.getSlashCommand(commandName);
                        if (command.isEmpty()) {
                            return reply(mce.getMessage(), "Command " + commandName + " does not exist.");
                        }
                        return getId(client)
                                .flatMap(id -> client.getRestClient().getApplicationService()
                                        .createGuildApplicationCommand(id, guildId, command.get().getRequest())
                                )
                                .then(reply(mce.getMessage(), "Command " + commandName + " updated."));
                    } else if ("!promoteCommand".equals(args[0])) {
                        // Push a single command to the world, and delete it from the guild.
                        var params = getGuildAndCommandNames(mce, args);
                        if (params.isEmpty()) {
                            return reply(mce.getMessage(), "Since you're in a DM, Guild ID is required for guild commands");
                        }
                        var guildId = params.get().getT1();
                        String commandName = params.get().getT2();
                        var command = registry.getSlashCommand(commandName);
                        if (command.isEmpty()) {
                            return reply(mce.getMessage(), "Command " + commandName + " does not exist.");
                        }
                        return getId(client)
                                .zipWith(findByName(client, commandName, guildId))
                                .flatMap(tuple -> client.getRestClient().getApplicationService()
                                        .deleteGuildApplicationCommand(tuple.getT1(), guildId, tuple.getT2().asLong())
                                        .thenReturn(tuple.getT1())
                                ).flatMap(id -> client.getRestClient().getApplicationService()
                                        .createGlobalApplicationCommand(id, command.get().getRequest())
                                )
                                .then(reply(mce.getMessage(), "Promoted command! May take a bit to be finished."));
                    } else {
                        return Flux.empty();
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Error thrown by command", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Mono<Long> getId(GatewayDiscordClient client) {
        return client.getRestClient().getApplicationId();
    }

    private Mono<Void> reply(Message message, String response) {
        return message.getChannel()
                .flatMap(mc -> mc.createMessage(MessageCreateSpec.builder()
                        .content(response)
                        .messageReference(message.getId())
                        .build()))
                .then();
    }

    private Optional<Long> getGuildIdFromCommand(MessageCreateEvent mce, String[] args) {
        if (args.length >= 2) {
            String guildId = args[1];
            return Optional.of(Long.parseLong(guildId));
        } else {
            return mce.getGuildId()
                    .map(Snowflake::asLong);
        }
    }

    private Optional<Tuple2<Long, String>> getGuildAndCommandNames(MessageCreateEvent mce, String[] args) {
        Optional<Long> guildId; String commandName;
        if (args.length == 2) {
            guildId = mce.getGuildId().map(Snowflake::asLong);
            commandName = args[1];
        } else if (args.length > 2) {
            guildId = Optional.of(Long.parseLong(args[1]));
            commandName = args[2];
        } else {
            guildId = Optional.empty();
            commandName = "";
        }
        return guildId.map(l -> Tuples.of(l, commandName));
    }

    private Mono<Id> findByName(GatewayDiscordClient client, String name, @Nullable Long guildId) {
        var as = client.getRestClient().getApplicationService();
        return getId(client)
                    .flatMapMany(id -> guildId == null ? as.getGlobalApplicationCommands(id) : as.getGuildApplicationCommands(id, guildId))
                    .filter(acd -> name.equals(acd.name()))
                    .next()
                    .map(ApplicationCommandData::id);
    }
}
