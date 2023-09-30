package com.github.milomarten.fracktail4.base.subcommand;

import com.github.milomarten.fracktail4.base.parameter.Parameters;
import reactor.core.publisher.Mono;

public interface Handler<CONTEXT> {
    Mono<?> handle(Parameters parameters, CONTEXT context);
}
