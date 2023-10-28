package com.github.milomarten.fracktail4.react;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ImmutableChannelData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractReactHandlerTest {
    @InjectMocks
    private AbstractReactHandler<Integer> handler = new AbstractReactHandlerTest.TestInstance();

    @Mock
    private GatewayDiscordClient client;

    @BeforeEach
    public void initialize() {
        handler.addDiscordHook(this.client);

        when(this.client.on(ReactionAddEvent.class)).thenReturn(Flux.empty());
        when(this.client.on(ReactionRemoveEvent.class)).thenReturn(Flux.empty());
        when(this.client.getChannelById(any()))
                .thenReturn(Mono.just(new TextChannel(this.client, ImmutableChannelData.builder()
                        .build())));
    }

    @Test
    public void publishNormalOptions() {
        var message = new ReactMessage<Integer>();
        message.setOptions(List.of(
                new ReactOption<>(0, ReactionEmoji.unicode("1"), "One"),
                new ReactOption<>(1, ReactionEmoji.unicode("2"), "Two"),
                new ReactOption<>(2, ReactionEmoji.unicode("3"), "Three")
        ));
    }

    private static class TestInstance extends AbstractReactHandler<Integer> {
        @Override
        protected Mono<Void> onReact(Member member, Integer integer) {
            return Mono.fromRunnable(() -> System.out.println("React:" + integer));
        }

        @Override
        protected Mono<Void> onUnreact(Member member, Integer integer) {
            return Mono.fromRunnable(() -> System.out.println("Unreact:" + integer));
        }

        @Override
        protected Mono<Void> updatePersistence() {
            return Mono.empty();
        }
    }
}