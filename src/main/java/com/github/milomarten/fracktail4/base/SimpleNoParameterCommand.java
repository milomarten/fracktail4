package com.github.milomarten.fracktail4.base;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a basic command which, when invoked, does some small logic before returning.
 * A command of this type would be suitable to operations like a randomized response, reading
 * from a local file, or querying another object's state.
 * On its own, a bean of this type does nothing. It is up to the platform to adapt a command
 * of this type into one that fits within it's platform's functionality.
 */
@RequiredArgsConstructor
@Getter
public abstract class SimpleNoParameterCommand {
    private final String name;
    private final String description;

    public abstract String getResponse();
}
