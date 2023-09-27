package com.github.milomarten.fracktail4.base.filter;

import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.base.Parameters;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.List;

public class CommandFilterChain {
    private final List<CommandFilter> rawList;
    private Iterator<CommandFilter> chain;

    public CommandFilterChain(List<CommandFilter> filters) {
        this.rawList = filters;
        this.chain = filters.iterator();
    }

    public Mono<Boolean> callNext(Command command, Parameters parameters, Object event) {
        if (chain.hasNext()) {
            CommandFilter next = chain.next();
            return next.filter(command, parameters, event, this);
        } else {
            this.chain = this.rawList.iterator();
            return Mono.just(true);
        }
    }
}
