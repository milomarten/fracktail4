package com.github.milomarten.fracktail4.permissions;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SetBasedRoleSet<ROLE> implements RoleSet<ROLE> {
    private final Set<ROLE> set;

    @Override
    public boolean hasRole(ROLE role) {
        return set.contains(role);
    }
}
