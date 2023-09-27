package com.github.milomarten.fracktail4.base;

import reactor.core.publisher.Mono;

public interface AllPlatformCommand extends Command {
    Mono<?> doCommand(Parameters parameters, Context context);
}
