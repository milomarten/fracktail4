package com.github.milomarten.fracktail4.base.filter;

import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.base.Parameters;
import reactor.core.publisher.Mono;

public interface CommandFilter {
    Mono<Boolean> filter(Command command, Parameters parameters, Object event, CommandFilterChain next);
}
