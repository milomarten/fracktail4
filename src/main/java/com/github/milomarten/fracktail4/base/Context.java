package com.github.milomarten.fracktail4.base;

import com.github.milomarten.fracktail4.permissions.Role;
import reactor.core.publisher.Mono;

public interface Context {
    Role getRole();

    Mono<?> respond(String response);

    default Mono<?> respondPrivately(String response) {
        return this.respond(response);
    }
}
