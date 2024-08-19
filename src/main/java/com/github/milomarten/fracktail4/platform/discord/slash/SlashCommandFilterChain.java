package com.github.milomarten.fracktail4.platform.discord.slash;

import com.github.milomarten.fracktail4.base.filter.CommandFilter;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;

@RequiredArgsConstructor
public class SlashCommandFilterChain {
    private final List<SlashCommandFilter> rawChain;
    private Iterator<SlashCommandFilter> iterator;

    public Mono<Boolean> callNext(ChatInputInteractionEvent event) {
        if (setUpIterator().hasNext()) {
            SlashCommandFilter next = iterator.next();
            return next.filter(event, this);
        } else {
            return Mono.just(true);
        }
    }

    private Iterator<SlashCommandFilter> setUpIterator() {
        if (iterator == null) {
            iterator = rawChain.iterator();
        }
        return iterator;
    }
}
