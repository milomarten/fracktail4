package com.github.milomarten.fracktail4.platform.discord.react;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageEditSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
class RoleDiscordInterface {
    private final GatewayDiscordClient gateway;

    public <ID> Mono<Snowflake> publishToDiscord(Snowflake channelId, String message, List<ReactOption<ID>> options) {
        return gateway.getChannelById(channelId)
                .cast(TextChannel.class)
                .flatMap(tc -> tc.createMessage(message))
                .flatMap(m -> {
                    return Flux.fromIterable(options)
                            .flatMap(ro -> m.addReaction(ro.getEmoji()))
                            .then(Mono.just(m.getId()));
                });
    }

    public <ID> Mono<Void> publishToDiscord(Snowflake channelId, Snowflake messageId, String message, Set<ReactionEmoji> toAdd) {
        return gateway.getMessageById(channelId, messageId)
                .flatMap(msg -> {
                    return msg.edit(MessageEditSpec.builder()
                            .contentOrNull(message)
                            .build());
                })
                .flatMap(msg -> {
                    return Flux.fromIterable(toAdd)
                            .flatMap(msg::addReaction)
                            .then(Mono.just(msg));
                })
                .then();
    }

    public Mono<Void> updateMessage(Snowflake channelId, Snowflake messageId, String message) {
        return gateway.getMessageById(channelId, messageId)
                .flatMap(msg -> {
                    return msg.edit(MessageEditSpec.builder()
                            .contentOrNull(message)
                            .build());
                })
                .then();
    }

    public Mono<Void> deleteMessage(Snowflake channelId, Snowflake messageId) {
        return this.gateway.getMessageById(channelId, messageId)
                .flatMap(m -> m.delete("React Complete"));
    }
}
