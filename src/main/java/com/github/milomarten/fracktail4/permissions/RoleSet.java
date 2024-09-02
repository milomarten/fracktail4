package com.github.milomarten.fracktail4.permissions;

import java.util.List;

public interface RoleSet<ROLE> {
    boolean hasRole(ROLE role);

    default boolean doesNotHaveRole(ROLE role) {
        return !hasRole(role);
    }

    default boolean hasAll(List<ROLE> roles) {
        return roles.stream()
                .allMatch(this::hasRole);
    }

    default boolean hasAny(List<ROLE> roles) {
        return roles.stream()
                .anyMatch(this::hasRole);
    }

    default boolean hasNone(List<ROLE> roles) {
        return roles.stream()
                .noneMatch(this::hasRole);
    }
}
