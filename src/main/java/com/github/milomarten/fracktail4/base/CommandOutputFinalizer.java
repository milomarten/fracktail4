package com.github.milomarten.fracktail4.base;

public interface CommandOutputFinalizer<EVENT> {
    String mapResponse(EVENT event, String response);

    static <T> CommandOutputFinalizer<T> getDefault() {
        return (event, response) -> response;
    }
}
