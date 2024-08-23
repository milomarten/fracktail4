package com.github.milomarten.fracktail4.permissions;

import java.util.Set;

public interface PermissionProvider<USER, ROLE> {
    Set<ROLE> getPermissionsForUser(USER user);
}
