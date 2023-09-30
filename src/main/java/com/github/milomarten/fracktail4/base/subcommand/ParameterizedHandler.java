package com.github.milomarten.fracktail4.base.subcommand;

import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.base.parameter.ParameterParser;
import reactor.core.publisher.Mono;

public record ParameterizedHandler<CONTEXT> (Handler<CONTEXT> handler, ParameterParser parser) implements Handler<CONTEXT> {
    @Override
    public Mono<?> handle(Parameters parameters, CONTEXT context) {
        return handler.handle(parameters, context);
    }
}
