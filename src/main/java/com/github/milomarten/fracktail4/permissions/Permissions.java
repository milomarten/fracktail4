package com.github.milomarten.fracktail4.permissions;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Set;

@RequiredArgsConstructor
public class Permissions<ROLE> {
    private final Set<ROLE> roles;

    public boolean hasRole(ROLE role) {
        return roles.contains(role);
    }

    public boolean doesNotHaveRole(ROLE role) {
        return !hasRole(role);
    }

    public boolean hasAny(ROLE... ors) {
        return Arrays.stream(ors)
                .anyMatch(roles::contains);
    }

    public boolean hasAll(ROLE... ands) {
        return Arrays.stream(ands)
                .allMatch(roles::contains);
    }

    public boolean hasNone(ROLE... nots) {
        return Arrays.stream(nots)
                .noneMatch(roles::contains);
    }
}
