package com.github.milomarten.fracktail4.permissions;

public interface PermissionsProvider<USER, ROLE> {
    RoleSet<ROLE> getRoles(USER user);
}
