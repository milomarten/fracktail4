package com.github.milomarten.fracktail4.base;

public interface CommandOutputFinalizer<EVENT, INPUT> {
    String mapResponse(EVENT event, INPUT response);

    static <T, I> CommandOutputFinalizer<T, String> getDefault() {
        return (event, response) -> response;
    }
}
