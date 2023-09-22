package com.github.milomarten.fracktail4.react;

import com.github.milomarten.fracktail4.react.roles.RoleReactMessage;
import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    }

    private Mono<Void> updatePersistence() {
        return this.persistence.store("role-reacts", this.roleReactMessages);
    }

    public Mono<Integer> publish(RoleReactMessage message) {
        if (message.getMessageId() == null) {
            return publishNew(message);
        } else {
            return Mono.error(new RuntimeException("Edit not supported yet. Soon!"));
        }
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
