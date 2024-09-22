package com.github.milomarten.fracktail4.platform.discord.slash;

import com.github.milomarten.fracktail4.base.*;
import com.github.milomarten.fracktail4.config.FracktailRoles;
import com.github.milomarten.fracktail4.permissions.PermissionsProvider;
import com.github.milomarten.fracktail4.platform.discord.DiscordHookSource;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.DiscordCommandOutputFinalizer;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SimpleCommandAsSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SimpleNoParameterAsyncCommandAsSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SimpleNoParameterCommandAsSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

@Component
@Slf4j
public class SlashCommandRegistry implements DiscordHookSource, BeanPostProcessor {
    private final PermissionsProvider<User, FracktailRoles> permissionsProvider;

    private final Map<String, SlashCommandWrapper> slashCommandLookup;
    private final Map<String, UserCommandWrapper> userCommandLookup;
    private final List<ApplicationCommandRequest> requests;
    private final List<SlashCommandFilter> filters;
    private final DiscordCommandOutputFinalizer finalizer;

    public SlashCommandRegistry(
            @Autowired(required = false) List<SlashCommandWrapper> slashCommands,
            @Autowired(required = false) List<UserCommandWrapper> userCommands,
            @Autowired(required = false) List<SlashCommandFilter> filters,
            PermissionsProvider<User, FracktailRoles> permissionsProvider,
            DiscordCommandOutputFinalizer commandFinalizer
    ) {
        this.slashCommandLookup = new HashMap<>();
        this.userCommandLookup = new HashMap<>();
        this.requests = new ArrayList<>();
        stream(slashCommands).forEach(this::addCommand);
        stream(userCommands).forEach(this::addCommand);
//        stream(simpleCommands)
//                .map(SimpleCommandAsSlashCommand::new)
//                .forEach(this::addCommand);

        this.filters = Objects.requireNonNullElseGet(filters, List::of);
        this.permissionsProvider = permissionsProvider;
        this.finalizer = commandFinalizer;
    }

    private static <T> Stream<T> stream(List<T> list) {
        if (list == null) return Stream.empty();
        else return list.stream();
    }

    public void addCommand(SlashCommandWrapper command) {
        var request = command.getRequest();
        if (request.name().isEmpty()) {
            throw new IllegalArgumentException("Command must have a name");
        }
        this.requests.add(request);
        if (this.slashCommandLookup.containsKey(request.name())) {
            log.warn("Overwriting slash command {}", request.name());
        }
        slashCommandLookup.put(request.name(), command);
        log.info("Added slash command {}", request.name());
    }

    public void addCommand(UserCommandWrapper command) {
        var request = command.getRequest();
        if (request.name().isEmpty()) {
            throw new IllegalArgumentException("Command must have a name");
        }
        this.requests.add(request);
        if (this.userCommandLookup.containsKey(request.name())) {
            log.warn("Overwriting user command {}", request.name());
        }
        userCommandLookup.put(request.name(), command);
        log.info("Added user command {}", request.name());
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SimpleCommand cmd) {
            addCommand(new SimpleCommandAsSlashCommand(cmd, getFinalizerForBean(cmd)));
        } else if (bean instanceof SimpleNoParameterCommand cmd) {
            addCommand(new SimpleNoParameterCommandAsSlashCommand(cmd, getFinalizerForBean(cmd)));
        } else if (bean instanceof SimpleNoParameterAsyncCommand cmd) {
            addCommand(new SimpleNoParameterAsyncCommandAsSlashCommand(cmd, getFinalizerForBean(cmd)));
        }

        return bean;
    }

    private CommandOutputFinalizer<ApplicationCommandInteractionEvent, String> getFinalizerForBean(Object bean) {
        return bean instanceof Translate ?
                this.finalizer : CommandOutputFinalizer.getDefault();
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(MessageCreateEvent.class).flatMap(mce -> {
            var message = mce.getMessage();
            var author = message.getAuthor();
            if (author.isEmpty() || permissionsProvider.getRoles(author.get()).doesNotHaveRole(FracktailRoles.OWNER)) {
                return Flux.empty();
            }
            String command = message.getContent();
            if (command.startsWith("!updateGuildCommands")) {
                Optional<Long> guildId = mce.getGuildId()
                        .map(Snowflake::asLong)
                        .or(() -> {
                            var split = command.split(" ", 2);
                            if (split.length < 2) {
                                return Optional.empty();
                            } else {
                                return Optional.of(split[1])
                                        .map(Long::parseLong);
                            }
                        });
                if (guildId.isEmpty()) {
                    return reply(message, "Since you're in a DM, you have to specify the Guild ID to do that.");
                }
                return getId(client)
                        .flatMapMany(id -> client.getRestClient().getApplicationService()
                                .bulkOverwriteGuildApplicationCommand(id, guildId.get(), requests)
                        )
                        .count()
                        .flatMap(count -> reply(message, "Updated " + count + " commands in the guild"));
            } else if ("!updateGlobalCommands".equals(command)){
                return getId(client)
                        .flatMapMany(id -> client.getRestClient().getApplicationService()
                                .bulkOverwriteGlobalApplicationCommand(id, requests)
                        )
                        .count()
                        .flatMap(count -> reply(message, "Updated " + count + " commands globally. Remember it may take a bit!"));
            } else {
                return Flux.empty();
            }
        })
        .onErrorResume(ex -> {
            log.error("Error thrown by command", ex);
            return Mono.empty();
        })
        .subscribe();

        client.on(ChatInputInteractionEvent.class).flatMap(acie -> {
            var name = acie.getCommandName();
            if (this.slashCommandLookup.containsKey(name)) {
                var chain = new SlashCommandFilterChain(this.filters);
                return chain.callNext(acie)
                        .flatMap(canUse -> {
                            if (canUse) { return this.slashCommandLookup.get(name).handleEvent(acie); }
                            else { return SlashCommands.replyEphemeral(acie, "That command can't be used now.");}
                        })
                        .then();
            } else {
                return acie.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .content("Unknown command " + name + ". Please contact the admin.")
                        .ephemeral(true)
                        .build()
                ).then();
            }
        })
        .onErrorResume(ex -> {
            log.error("Error thrown by command", ex);
            return Mono.empty();
        })
        .subscribe();

        client.on(UserInteractionEvent.class).flatMap(acie -> {
            var name = acie.getCommandName();
            if (this.userCommandLookup.containsKey(name)) {
                return this.userCommandLookup.get(name).handleEvent(acie);
            } else {
                return acie.reply(InteractionApplicationCommandCallbackSpec.builder()
                        .content("Unknown command " + name + ". Please contact the admin.")
                        .ephemeral(true)
                        .build()
                ).then();
            }
        })
        .onErrorResume(ex -> {
            log.error("Error thrown by user command", ex);
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
}
