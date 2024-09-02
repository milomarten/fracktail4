package com.github.milomarten.fracktail4.permissions;

import lombok.Builder;
import lombok.Singular;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Builder
public class RuleBasedPermissionProvider<USER, ROLE> implements PermissionsProvider<USER, ROLE> {
    @Singular private final List<RoleRule<USER, ROLE>> rules;
    private final Supplier<? extends Set<ROLE>> finisher;

    @Override
    public RoleSet<ROLE> getRoles(USER user) {
        Set<ROLE> set = rules.stream()
                .filter(rule -> rule.test(user))
                .map(RoleRule::rule)
                .collect(Collectors.toCollection(finisher));
        return new SetBasedRoleSet<>(set);
    }
}
