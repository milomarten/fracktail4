package com.github.milomarten.fracktail4.persistence;

import reactor.core.publisher.Mono;

/**
 * Indicates that a bean uses persistence in some form.
 * Specifically, this is useful if you need some global way to update all persistence.
 */
public interface PersistenceBean {
    Mono<Void> load();
    Mono<Void> store();
}
