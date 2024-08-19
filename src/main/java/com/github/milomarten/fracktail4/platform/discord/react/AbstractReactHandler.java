package com.github.milomarten.fracktail4.platform.discord.react;

import com.github.milomarten.fracktail4.platform.discord.DiscordHookSource;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractReactHandler<ID> implements DiscordHookSource {
    @Getter
    private final List<ReactMessage<ID>> roleReactMessages = new ArrayList<>();

    protected GatewayDiscordClient gateway;

    protected RoleDiscordInterface connector;

    protected abstract Mono<Void> onReact(Member member, ID id);

    protected abstract Mono<Void> onUnreact(Member member, ID id);

    @Override
    public void addDiscordHook(GatewayDiscordClient client){
        this.gateway = client;
        this.connector = new RoleDiscordInterface(client);
        this.gateway.on(ReactionAddEvent.class)
                .flatMap(rae -> {
                    return findMatchingRoleReact(rae.getMessageId(), rae.getEmoji())
                            .map(match -> Mono.just(Tuples.of(rae, match)))
                            .orElseGet(Mono::empty);
                })
                .flatMap(tuple -> {
                    var event = tuple.getT1();
                    // This *shouldn't* throw...all role reacts should happen out of DMs
                    var member = event.getMember().orElseThrow();
                    return onReact(member, tuple.getT2().getId());
                })
                .onErrorResume(ex -> {
                    log.error("Uncaught exception", ex);
                    return Mono.empty();
                })
                .subscribe();

        this.gateway.on(ReactionRemoveEvent.class)
                .flatMap(rre -> {
                    return findMatchingRoleReact(rre.getMessageId(), rre.getEmoji())
                            .map(match -> Mono.just(Tuples.of(rre, match)))
                            .orElseGet(Mono::empty);
                })
                .flatMap(tuple -> {
                    var event = tuple.getT1();
                    return client.getMemberById(
                                    event.getGuildId().orElseThrow(),
                                    event.getUserId()
                            )
                            .flatMap(member -> onUnreact(member, tuple.getT2().getId()));
                })
                .onErrorResume(ex -> {
                    log.error("Uncaught exception", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Optional<ReactOption<ID>> findMatchingRoleReact(Snowflake messageId, ReactionEmoji react) {
        return this.roleReactMessages
                .stream()
                .filter(rrm -> rrm.getMessageId().equals(messageId))
                .findFirst()
                .flatMap(rrm -> {
                    return rrm.getOptions()
                            .stream()
                            .filter(ro -> ro.getEmoji().equals(react))
                            .findFirst();
                });
    }

    protected abstract Mono<Void> updatePersistence();

    public Mono<Integer> publish(ReactMessage<ID> message) {
        if (message.getMessageId() != null) {
            for (int idx = 0; idx < this.roleReactMessages.size(); idx++) {
                var rrm = this.roleReactMessages.get(idx);
                if (rrm.getMessageId().equals(message.getMessageId())) {
                    return publishEdit(rrm, message, idx);
                }
            }
        }
        return publishNew(message);
    }

    private Mono<Integer> publishNew(ReactMessage<ID> message) {
        if (message.getOptions().isEmpty()) {
            return Mono.empty();
        }

        if (message.hasMoreThan20Options()) {
            var second = message.splitOffExcessChoices(); // Reduce message to exactly 20 options, pushing the rest into a new ReactMessage
            // Split into two or more messages, and marking the links appropriately.
            return publishNew(message) // Publish the first. This is guaranteed to not recurse.
                    .flatMap(firstIdx -> publishNew(second) // Publish the second. This may recurse in extreme cases.
                            .doOnSuccess(message::setLink) // Set the first message's link to the second message.
                            .flatMap(n -> {
                                String messageBody = getMessageBody(message); // Update the first's text message with a link to the second
                                return connector.updateMessage(message.getChannelId(), message.getMessageId(), messageBody);
                            })
                            .thenReturn(firstIdx)); // Return the root.
        }

        String messageBody = getMessageBody(message);

        return connector.publishToDiscord(message.getChannelId(), messageBody, message.getOptions())
                .flatMap(id -> {
                    message.setMessageId(id);
                    this.roleReactMessages.add(message);
                    return updatePersistence().thenReturn(this.roleReactMessages.size() - 1);
                });
    }

    private Mono<Integer> publishEdit(ReactMessage<ID> old, ReactMessage<ID> nu, int idx) {
        if (nu.hasNoOptions()) {
            return deleteById(idx)
                    .then(Mono.empty());
        } else if (nu.hasMoreThan20Options()) {
            if (old.isLinked()) {
                // If you exceed 20 options on something already linked, we need to do more complex
                // logic to handle that. For now, throw an exception (to add more, edit the linked one)
                return Mono.error(new IllegalStateException("I don't know how to do that yet. Try editing " + old.getLink() + " instead"));
            }
            else {
                // The new message pushed the choices past 20.
                // Update the existing one, then create a new one with the remaining choices.
                ReactMessage<ID> secondNu = nu.splitOffExcessChoices();
                return publishEdit(old, nu, idx)
                        .flatMap(firstIdx -> publishNew(secondNu)
                                .doOnSuccess(nu::setLink)
                                .flatMap(n -> {
                                    String messageBody = getMessageBody(nu); // Update the first's text message with a link to the second
                                    return connector.updateMessage(nu.getChannelId(), nu.getMessageId(), messageBody);
                                })
                                .thenReturn(firstIdx));
            }
        }

        String messageBody = getMessageBody(nu);
        Set<ReactionEmoji> oldEmoji = getOptionsAsSet(old); // [A, B, C]
        Set<ReactionEmoji> newEmoji = getOptionsAsSet(nu); // [B, C, D]

        var toAdd = SetUtils.difference(newEmoji, oldEmoji); // [ D ]

        return connector.publishToDiscord(nu.getChannelId(), nu.getMessageId(), messageBody, toAdd)
                .thenReturn(idx)
                .flatMap(i -> {
                    this.roleReactMessages.set(i, nu);
                    return updatePersistence().thenReturn(i);
                });
    }

    private String getMessageBody(ReactMessage<ID> message) {
        var list = message.getOptions().stream()
                .map(ro -> String.format("* %s - %s", getFormattedEmoji(ro.getEmoji()), ro.getDescription()))
                .collect(Collectors.joining("\n"));

        List<String> body = new ArrayList<>();
        if (message.getDescription() != null && !message.getDescription().isBlank()) {
            body.add(message.getDescription());
        }
        if (message.isLinked()) {
            getById(message.getLink())
                    .map(next -> String.format("https://discord.com/channels/%s/%s/%s",
                            next.getGuildId().asString(),
                            next.getChannelId().asString(),
                            next.getMessageId().asString()
                    ))
                    .ifPresent(linkUrl -> {
                        body.add("More choices can be found here: " + linkUrl);
                    });
        }
        body.add(list);

        return String.join("\n\n", body);
    }

    private String getFormattedEmoji(ReactionEmoji emoji) {
        return emoji.asCustomEmoji()
                .map(ReactionEmoji.Custom::asFormat)
                .or(() -> emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw))
                .orElse("???");
    }

    public Optional<ReactMessage<ID>> getById(int id) {
        if (id < 0 || id >= this.roleReactMessages.size()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(this.roleReactMessages.get(id));
        }
    }

    public Mono<Void> deleteById(int id) {
        if (id < 0 || id >= this.roleReactMessages.size()) {
            return Mono.empty();
        }
        var message = this.roleReactMessages.get(id);
        if (message == null) {
            return Mono.empty();
        }

        return connector.deleteMessage(message.getChannelId(), message.getMessageId())
                .doOnSuccess(e -> {
                    this.roleReactMessages.set(id, null);
                }) // Preserves subsequent IDs
                // Update any links
                .thenMany(Flux.fromStream(this.roleReactMessages.stream().filter(Objects::nonNull)))
                .filter(rm -> rm.getLink() == id)
                .flatMap(rm -> {
                    rm.unlink();
                    String messageBody = getMessageBody(rm);
                    return connector.updateMessage(rm.getChannelId(), rm.getMessageId(), messageBody);
                })
                .then(updatePersistence());
    }

    private Set<ReactionEmoji> getOptionsAsSet(ReactMessage<ID> old) {
        return old.getOptions()
                .stream()
                .map(ReactOption::getEmoji)
                .collect(Collectors.toSet());
    }
}
