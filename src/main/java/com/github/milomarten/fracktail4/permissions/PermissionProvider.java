package com.github.milomarten.fracktail4.permissions;

import java.util.Set;

/**
 * A generic permissions provider, which gets roles associated with a bot user.
 * Each platform should create some implementation of this class, using their specific flavor of user object.
 * Roles are arbitrary on a bot-by-bot basis, but should attempt to be as common as possible.
 * I included ROLE for type safety, but it may just be that a String is a better universal option.
 * @param <USER> The type of the user
 * @param <ROLE> The type of the role. An Enum usually fits best here.
 */
public interface PermissionProvider<USER, ROLE> {
    /**
     * Get the permissions a user has
     * @param user The user to check
     * @return The user's permissions
     */
    Permissions<ROLE> getPermissionsForUser(USER user);
}
