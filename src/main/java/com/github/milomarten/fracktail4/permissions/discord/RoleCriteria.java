package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.permissions.Role;
import lombok.Data;

import java.util.List;

@Data
public class RoleCriteria {
    private Role defaultRole = Role.BLOCKED;
    private List<RoleMapping> mappings;
}
