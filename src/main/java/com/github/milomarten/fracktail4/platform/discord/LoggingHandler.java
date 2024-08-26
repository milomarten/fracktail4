package com.github.milomarten.fracktail4.platform.discord;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.Event;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingHandler implements DiscordHookSource {
    @Override
    public void addDiscordHook(GatewayDiscordClient client) {
        client.on(new ReactiveEventAdapter() {
            @Override
            public Publisher<?> hookOnEvent(Event event) {
                log.info("Recieved event: {}", event.getClass());
                return Mono.empty();
            }
        });
    }
}
