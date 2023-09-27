package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.base.filter.CommandFilterChain;
import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
import com.github.milomarten.fracktail4.permissions.discord.RoleEngine;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@Component
public class CommandRegistry implements DiscordHookSource {
    private final CommandConfiguration configuration;
    private final RoleEngine roleEngine;
    private final CommandFilterChain chain;
    private Map<String, Command> commands;
    private Map<String, Command> commandsByAlias;

    public CommandRegistry(List<CommandBundle> bundles, List<Command> commands,
                           CommandConfiguration configuration, RoleEngine roleEngine, CommandFilterChain chain) {
        this.commands = new HashMap<>();
        this.commandsByAlias = new HashMap<>();
        this.configuration = configuration;
        this.roleEngine = roleEngine;
        this.chain = chain;

        bundles.stream()
            .flatMap(bundle -> bundle.getCommands().stream())
            .forEach(this::registerCommand);
        commands.forEach(this::registerCommand);
    }

    public void registerCommand(Command command) {
        CommandData cd = command.getCommandData();
        var old = this.commands.put(cd.getId(), command);
        log.info("Registering command {} for type {}", cd.getId(), command.getClass().getSimpleName());
        if (old != null) {
            log.warn("Encountered two commands with shared ID {}. Overwriting {} with {}", cd.getId(), old, command);
        }
        for (String alias : cd.getAliases()) {
            var oldAlias = this.commandsByAlias.put(alias, command);
            if (oldAlias != null) {
                log.warn("Encountered two commands with shared alias {}. Overwriting command {} with {}",
                        alias, oldAlias.getCommandData().getId(), cd.getId());
            }
        }

        if (command instanceof CommandRegistryAware aware) {
            aware.setCommandRegistry(this);
        }
    }

    public Optional<Command> lookupByAlias(String alias) {
        return Optional.ofNullable(this.commandsByAlias.get(alias));
    }

    public Optional<Command> lookupByAliasAndRole(String alias, Member member) {
        return lookupByAlias(alias)
                .filter(cmd -> roleEngine.canUseCommand(member, cmd));
    }

    public Optional<Command> lookupByAliasAndRole(String alias, User user) {
        return lookupByAlias(alias)
                .filter(cmd -> roleEngine.canUseCommand(user, cmd));
    }

    public Collection<Command> getCommands() {
        return this.commands.values();
    }

    public Collection<Command> getUsableCommands(Member member) {
        return this.commands.values()
                .stream()
                .filter(cmd -> roleEngine.canUseCommand(member, cmd))
                .collect(Collectors.toCollection(List::of));
    }

    public Collection<Command> getUsableCommands(User user) {
        return this.commands.values()
                .stream()
                .filter(cmd -> roleEngine.canUseCommand(user, cmd))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.getEventDispatcher().on(MessageCreateEvent.class)
                .filter(mce -> isRealPerson(mce.getMessage()))
                .flatMap(mce -> {
                    var match = getMatchedCommand(mce.getMessage());
                    if (match != null && match.getT1() instanceof DiscordCommand dc) {
                        Parameters params = match.getT1()
                                .getCommandData()
                                .getParameterParser()
                                .parse(this.configuration, match.getT2());

                        return Mono.just(Tuples.of(mce, dc, params));
                    }
                    return Mono.empty();
                })
                .filter(tuple -> {
                    var mce = tuple.getT1();
                    var cmd = tuple.getT2();

                    return mce.getMember()
                            .map(member -> roleEngine.canUseCommand(member, cmd))
                            .or(() -> mce.getMessage().getAuthor().map(user -> roleEngine.canUseCommand(user, cmd)))
                            .orElse(false);
                })
                .filterWhen(tuple -> chain.callNext(tuple.getT2(), tuple.getT3(), tuple.getT1()))
                .flatMap(tuple -> {
                    var mce = tuple.getT1();
                    var cmd = tuple.getT2();
                    var params = tuple.getT3();

                    return cmd.doCommand(params, mce);
                })
                .onErrorResume(ex -> {
                    log.error("Encountered uncaught error", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private boolean isRealPerson(Message message) {
        return message.getAuthor()
                .map(u -> !u.isBot())
                .orElse(false);
    }

    private Tuple2<Command, String> getMatchedCommand(Message message) {
        var tokens = message.getContent().split(configuration.getDelimiter(), 2);
        if (tokens[0].startsWith(configuration.getPrefix())) {
            var commandString = tokens[0].substring(1);
            if (this.commandsByAlias.containsKey(commandString)) {
                return Tuples.of(this.commandsByAlias.get(commandString), tokens.length == 1 ? "" : tokens[1]);
            } else {
                return null;
            }
        }
        return null;
    }
}
