package com.github.milomarten.fracktail4.react;

import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageEditSpec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractReactHandler<ID> implements DiscordHookSource {
    @Getter
    private final List<ReactMessage<ID>> roleReactMessages = new ArrayList<>();

    protected GatewayDiscordClient gateway;

    protected abstract Mono<Void> onReact(Member member, ID id);

    protected abstract Mono<Void> onUnreact(Member member, ID id);

    @Override
    public void addDiscordHook(GatewayDiscordClient client){
        this.gateway = client;
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
        String messageBody = getMessageBody(message);

        return gateway.getChannelById(message.getChannelId())
                .cast(TextChannel.class)
                .flatMap(tc -> tc.createMessage(messageBody))
                .flatMap(m -> {
                    message.setMessageId(m.getId());
                    return Flux.fromIterable(message.getOptions())
                            .flatMap(ro -> m.addReaction(ro.getEmoji()))
                            .then(Mono.just("COMPLETE"));
                })
                .flatMap(vod -> {
                    this.roleReactMessages.add(message);
                    return updatePersistence().thenReturn(this.roleReactMessages.size() - 1);
                });
    }

    private Mono<Integer> publishEdit(ReactMessage<ID> old, ReactMessage<ID> nu, int idx) {
        String messageBody = getMessageBody(nu);
        Set<ReactionEmoji> oldEmoji = old.getOptions()
                .stream()
                .map(ReactOption::getEmoji)
                .collect(Collectors.toSet()); // [A, B, C]
        Set<ReactionEmoji> newEmoji = nu.getOptions()
                .stream()
                .map(ReactOption::getEmoji)
                .collect(Collectors.toSet()); // [B, C, D]

        var toAdd = SetUtils.difference(newEmoji, oldEmoji); // [ D ]

        return gateway.getMessageById(nu.getChannelId(), nu.getMessageId())
                .flatMap(msg -> {
                    return msg.edit(MessageEditSpec.builder()
                        .contentOrNull(messageBody)
                        .build());
                })
                .flatMap(msg -> {
                    return Flux.fromIterable(toAdd)
                                .flatMap(msg::addReaction)
                                .then(Mono.just(msg));
                })
                .flatMap(vod -> {
                    this.roleReactMessages.set(idx, nu);
                    return updatePersistence().thenReturn(idx);
                });
    }

    private String getMessageBody(ReactMessage<ID> message) {
        var list = message.getOptions().stream()
                .map(ro -> String.format("* %s - %s", getFormattedEmoji(ro.getEmoji()), ro.getDescription()))
                .collect(Collectors.joining("\n"));
        if (message.getDescription() == null || message.getDescription().isBlank()) {
            return list;
        } else {
            return message.getDescription() + "\n" + list;
        }
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

    public Mono<Integer> editById(int id, UnaryOperator<ReactMessage<ID>> editFunc) {
        return Mono.justOrEmpty(getById(id))
                .flatMap(rm -> {
                    var newRm = editFunc.apply(rm);
                    if (newRm == null) {
                        return Mono.empty();
                    }
                    return publish(newRm);
                });
    }

    public Mono<Void> deleteById(int id) {
        if (id < 0 || id >= this.roleReactMessages.size()) {
            return Mono.empty();
        }
        var message = this.roleReactMessages.get(id);
        if (message == null) {
            return Mono.empty();
        }

        return this.gateway.getMessageById(message.getChannelId(), message.getMessageId())
                .flatMap(m -> m.delete("React Complete"))
                .doOnSuccess(e -> this.roleReactMessages.set(id, null)) // Preserves subsequent IDs
                .flatMap(e -> updatePersistence());
    }
}
