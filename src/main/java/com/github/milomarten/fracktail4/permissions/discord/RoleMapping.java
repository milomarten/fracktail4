package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.permissions.Role;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lombok.Data;

@Data
public class RoleMapping {
    private Long roleId;
    private Long userId;
    private Role role;

    public Snowflake getRoleIdSnowflake() {
        return this.roleId == null ? null : Snowflake.of(this.roleId);
    }

    public Snowflake getUserIdSnowflake() {
        return this.userId == null ? null : Snowflake.of(this.userId);
    }

    public boolean matches(Member member) {
        Snowflake role = getRoleIdSnowflake();
        Snowflake user = getUserIdSnowflake();
        boolean roleMatch = role == null || member.getRoleIds().contains(role);
        boolean userMatch = user == null || member.getId().equals(user);
        return roleMatch && userMatch;
    }

    public boolean matches (User user) {
        Snowflake userId = getUserIdSnowflake();
        boolean roleMatch = (roleId == null);
        boolean userMatch = userId == null || user.getId().equals(userId);
        return roleMatch && userMatch;
    }
}
