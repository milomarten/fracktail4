package com.github.milomarten.fracktail4.react;

import com.github.milomarten.fracktail4.react.roles.RoleReactMessage;
import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageEditSpec;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RoleHandler implements DiscordHookSource {
    @Autowired
    private Persistence persistence;

    @Getter
    private List<RoleReactMessage> roleReactMessages;

    private GatewayDiscordClient gateway;

    @PostConstruct
    public void load() {
        var reacts = persistence.retrieve("role-reacts", RoleReactMessage[].class)
                .block();
        log.info("Loaded {} existing role reacts from persistence", reacts == null ? 0 : reacts.length);
        if (reacts == null) {
            this.roleReactMessages = new ArrayList<>();
        } else {
            this.roleReactMessages = new ArrayList<>(List.of(reacts));
        }
    }

    @Override
    public void addDiscordHook(GatewayDiscordClient client){
        this.gateway = client;
        this.gateway.on(ReactionAddEvent.class)
                .flatMap(rae -> {
                    return findMatchingRoleReact(rae.getMessageId())
                            .map(match -> Mono.just(Tuples.of(rae, match)))
                            .orElseGet(Mono::empty);
                })
                .flatMap(tuple -> {
                    var event = tuple.getT1();
                    var choiceMaybe = getMatchingOption(event.getEmoji(), tuple.getT2());
                    if (choiceMaybe.isEmpty()) {
                        return Mono.empty();
                    } else {
                        // This *shouldn't* throw...all role reacts should happen out of DMs
                        var member = event.getMember().orElseThrow();
                        return member.addRole(choiceMaybe.get().getId(), "Self-assign");
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Uncaught exception", ex);
                    return Mono.empty();
                })
                .subscribe();

        this.gateway.on(ReactionRemoveEvent.class)
                .flatMap(rre -> {
                    return findMatchingRoleReact(rre.getMessageId())
                            .map(match -> Mono.just(Tuples.of(rre, match)))
                            .orElseGet(Mono::empty);
                })
                .flatMap(tuple -> {
                    var event = tuple.getT1();
                    var choiceMaybe = getMatchingOption(event.getEmoji(), tuple.getT2());
                    return choiceMaybe.map(snowflakeReactOption -> event.getClient().getMemberById(
                                    event.getGuildId().orElseThrow(),
                                    event.getUserId())
                            .flatMap(member -> member.removeRole(snowflakeReactOption.getId(), "Self-unassign")))
                            .orElseGet(Mono::empty);
                })
                .onErrorResume(ex -> {
                    log.error("Uncaught exception", ex);
                    return Mono.empty();
                })
                .subscribe();
    }

    private Optional<RoleReactMessage> findMatchingRoleReact(Snowflake message) {
        return this.roleReactMessages
                .stream()
                .filter(rrm -> rrm.getMessageId().equals(message))
                .findFirst();
    }

    private static Optional<ReactOption<Snowflake>> getMatchingOption(ReactionEmoji emoji, RoleReactMessage react) {
        return react
                .getOptions()
                .stream()
                .filter(ro -> ro.getEmoji().equals(emoji))
                .findFirst();
    }

    private Mono<Void> updatePersistence() {
        return this.persistence.store("role-reacts", this.roleReactMessages);
    }

    public Mono<Integer> publish(RoleReactMessage message) {
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

    private Mono<Integer> publishNew(RoleReactMessage message) {
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

    public Mono<Integer> publishEdit(RoleReactMessage old, RoleReactMessage nu, int idx) {
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

    private String getMessageBody(RoleReactMessage message) {
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

    public Optional<RoleReactMessage> getById(int id) {
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

        return this.gateway.getMessageById(message.getChannelId(), message.getMessageId())
                .flatMap(m -> m.delete("React Complete"))
                .doOnSuccess(e -> this.roleReactMessages.set(id, null)) // Preserves subsequent IDs
                .flatMap(e -> updatePersistence());
    }
}
