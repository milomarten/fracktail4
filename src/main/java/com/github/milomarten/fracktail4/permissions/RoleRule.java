package com.github.milomarten.fracktail4.permissions;

import java.util.function.Predicate;

public record RoleRule<USER, ROLE>(ROLE rule, Predicate<USER> matcher) implements Predicate<USER> {
    @Override
    public boolean test(USER user) {
        return this.matcher.test(user);
    }
}
