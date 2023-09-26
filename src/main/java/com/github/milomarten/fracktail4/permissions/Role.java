package com.github.milomarten.fracktail4.permissions;

public enum Role {
    ADMIN,
    MODERATOR,
    NORMAL,
    BLOCKED;

    public boolean meetsOrExceeds(Role compare) {
        return this.ordinal() <= compare.ordinal();
    }
}
