package com.github.milomarten.fracktail4.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * Represents a basic command which, when invoked, does some complex logic before returning.
 * This type of command is similar to a SimpleNoParameterCommand, but supports an asynchronous
 * response. This allows you to query a database, for example. It is up to the developer to determine
 * if a response should be asynchronous or not. Note that some platforms require some sort of acknowledgement
 * if a response will take a significant amount of time. In those cases, the SimpleNoParameterAsync
 * command will perform the necessary handling.
 * On its own, a bean of this type does nothing. It is up to the platform to adapt a command
 * of this type into one that fits within it's platform's functionality.
 */
@RequiredArgsConstructor
@Getter
public abstract class SimpleNoParameterAsyncCommand {
    private final String name;
    private final String description;

    public abstract Mono<String> getResponse();
}
