package com.github.milomarten.fracktail4.platform.discord.slash;

import com.github.milomarten.fracktail4.base.SimpleCommand;
import com.github.milomarten.fracktail4.platform.discord.DiscordHookSource;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

@Component
@Slf4j
public class SlashCommandRegistry implements DiscordHookSource, BeanPostProcessor {
    @Value("${discord.slash.guildId}")
    private Snowflake guildId;

    @Value("${discord.slash.updateCommand.permissibleUsers}")
    private Set<Long> updateCommandUsers;

    private final Map<String, SlashCommandWrapper> lookup;
    private final List<ApplicationCommandRequest> requests;
    private final List<SlashCommandFilter> filters;

    public SlashCommandRegistry(
            @Autowired(required = false) List<SlashCommandWrapper> slashCommands,
            @Autowired(required = false) List<SimpleCommand> simpleCommands,
            @Autowired(required = false) List<SlashCommandFilter> filters
    ) {
        this.lookup = new HashMap<>();
        this.requests = new ArrayList<>();
        stream(slashCommands).forEach(this::addCommand);
        stream(simpleCommands)
                .map(SimpleCommandAsSlashCommand::new)
                .forEach(this::addCommand);

        this.filters = Objects.requireNonNullElseGet(filters, List::of);
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
        if (this.lookup.containsKey(request.name())) {
            log.warn("Overwriting command {}", request.name());
        }
        lookup.put(request.name(), command);
        log.info("Added command {}", request.name());
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(MessageCreateEvent.class).flatMap(mce -> {
            var message = mce.getMessage();
            var author = message.getAuthor();
            if (author.isEmpty() || !updateCommandUsers.contains(author.get().getId().asLong())) {
                return Flux.empty();
            }
            String command = message.getContent();
            if ("!updateGuidSlashCommands".equals(command)) {
                return getId(client)
                        .flatMapMany(id -> client.getRestClient().getApplicationService()
                                .bulkOverwriteGuildApplicationCommand(id, guildId.asLong(), requests)
                        )
                        .count()
                        .flatMap(count -> reply(message, "Updated " + count + " commands in the guild"));
            } else if ("!updateGlobalSlashCommands".equals(command)){
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
        .subscribe();

        client.on(ChatInputInteractionEvent.class).flatMap(acie -> {
            var name = acie.getCommandName();
            if (this.lookup.containsKey(name)) {
                var chain = new SlashCommandFilterChain(this.filters);
                return chain.callNext(acie)
                        .flatMap(canUse -> {
                            if (canUse) { return this.lookup.get(name).handleEvent(acie); }
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
        .onErrorContinue((ex, obj) -> log.error("Error thrown by command", ex))
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
