package com.github.milomarten.fracktail4.react;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.reaction.ReactionEmoji;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractReactHandlerTest {
    @InjectMocks
    private AbstractReactHandler<Integer> handler = new AbstractReactHandlerTest.TestInstance();
    @Mock
    private RoleDiscordInterface connector;

    @Test
    public void publishNewWithNoOptions() {
        ReactMessage<Integer> message = createReact(null, 0);

        StepVerifier.create(handler.publish(message))
                .verifyComplete();

        assertEquals(0, handler.getRoleReactMessages().size());
    }

    @Test
    public void publishNewWithNormalOptions() {
        ReactMessage<Integer> message = createReact(null, 3);

        when(connector.publishToDiscord(any(), anyString(), anyList()))
                .thenReturn(Mono.just(Snowflake.of(Instant.now())));

        StepVerifier.create(handler.publish(message))
                .expectNext(0)
                .verifyComplete();

        var created = handler.getById(0).get();
        assertEquals(3, created.getOptions().size());
        assertNotNull(created.getMessageId());
        assertFalse(created.isLinked());
    }

    @Test
    public void publishNewWithManyOptions() {
        ReactMessage<Integer> message = createReact(null, 25);

        when(connector.publishToDiscord(any(), anyString(), anyList()))
                .thenReturn(Mono.just(Snowflake.of(Instant.now())));
        when(connector.updateMessage(any(), any(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(message))
                .expectNext(0)
                .verifyComplete();

        assertEquals(2, handler.getRoleReactMessages().size());
        var first = handler.getById(0).get();
        assertEquals(1, first.getLink());
        assertEquals(20, first.getOptions().size());

        var second = handler.getById(1).get();
        assertEquals(5, second.getOptions().size());

        verify(connector, times(2)).publishToDiscord(any(), anyString(), anyList());
        verify(connector).updateMessage(any(), any(), anyString());
    }

    @Test
    public void publishEditWithNormalOptions_AddOptions() {
        ReactMessage<Integer> original = createReact(id(), 3);
        handler.getRoleReactMessages().add(original);

        ReactMessage<Integer> patch = createReact(original.getMessageId(), 4);

        when(connector.publishToDiscord(any(), any(), anyString(), anySet()))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(patch))
                .expectNext(0)
                .verifyComplete();

        assertEquals(1, handler.getRoleReactMessages().size());
        var created = handler.getById(0).get();
        assertEquals(4, created.getOptions().size());
        assertNotNull(created.getMessageId());
        assertFalse(created.isLinked());
    }

    @Test
    public void publishEditWithNormalOptions_RemoveOptions() {
        ReactMessage<Integer> original = createReact(id(), 3);
        handler.getRoleReactMessages().add(original);

        ReactMessage<Integer> patch = createReact(original.getMessageId(), 2);

        when(connector.publishToDiscord(any(), any(), anyString(), eq(Set.of())))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(patch))
                .expectNext(0)
                .verifyComplete();

        assertEquals(1, handler.getRoleReactMessages().size());
        var created = handler.getById(0).get();
        assertEquals(2, created.getOptions().size());
        assertNotNull(created.getMessageId());
        assertFalse(created.isLinked());
    }

    @Test
    public void publishEditWithNormalOptions_NoOptions() {
        ReactMessage<Integer> original = createReact(id(), 3);
        handler.getRoleReactMessages().add(original);

        ReactMessage<Integer> patch = createReact(original.getMessageId(), 0);

        when(connector.deleteMessage(any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(patch))
                .verifyComplete();

        assertEquals(1, handler.getRoleReactMessages().size());
        assertNull(handler.getRoleReactMessages().get(0));
    }

    @Test
    public void publishEditWithManyOptions_Old20_New21_AddOptions() {
        ReactMessage<Integer> original = createReact(id(), 20);
        handler.getRoleReactMessages().add(original);

        ReactMessage<Integer> patch = createReact(original.getMessageId(), 21);

        when(connector.publishToDiscord(any(), any(), anyString(), anySet()))
                .thenReturn(Mono.empty());
        when(connector.publishToDiscord(any(), any(), anyList()))
                .thenReturn(Mono.just(id()));
        when(connector.updateMessage(any(), any(), anyString()))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(patch))
                .expectNext(0)
                .verifyComplete();

        assertEquals(2, handler.getRoleReactMessages().size());
        var created = handler.getById(0).get();
        assertEquals(20, created.getOptions().size());
        assertEquals(1, created.getLink());

        var created2 = handler.getById(1).get();
        assertEquals(1, created2.getOptions().size());
        assertFalse(created2.isLinked());
    }

    @Test
    public void publishEditWithNormalOptions_ZeroOptions_UpdateLink() {
        ReactMessage<Integer> original = createReact(id(), 20);
        original.setLink(1);
        handler.getRoleReactMessages().add(original);

        ReactMessage<Integer> second = createReact(id(), 1);
        handler.getRoleReactMessages().add(second);

        ReactMessage<Integer> secondPatch = createReact(second.getMessageId(), 0);

        when(connector.updateMessage(any(), any(), anyString()))
                .thenReturn(Mono.empty());
        when(connector.deleteMessage(any(), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(handler.publish(secondPatch))
                .verifyComplete();

        assertEquals(2, handler.getRoleReactMessages().size());
        var created = handler.getById(0).get();
        assertEquals(20, created.getOptions().size());
        assertFalse(created.isLinked());

        assertTrue(handler.getById(1).isEmpty());
    }

    private static Snowflake id() {
        return Snowflake.of(Instant.now());
    }

    private static ReactMessage<Integer> createReact(Snowflake messageId, int numOptions) {
        var message = new ReactMessage<Integer>();
        message.setGuildId(Snowflake.of(1));
        message.setChannelId(Snowflake.of(2));
        message.setMessageId(messageId);
        var options = IntStream.range(0, numOptions)
                .mapToObj(i -> new ReactOption<>(i, ReactionEmoji.unicode(String.valueOf(i)), "Choice " + i))
                .toList();
        message.setOptions(options);
        return message;
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