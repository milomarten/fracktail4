package com.github.milomarten.fracktail5.platform.text;

import reactor.core.publisher.Mono;

public interface TextCommandResponse<T extends TextCommandContext> {
    Mono<?> respond(T context);
}
