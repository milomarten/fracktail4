package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.permissions.Role;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "roles.discord")
@Data
public class RoleConfiguration {
    private RoleCriteria dm;
    private List<GuildBasedRoleCriteria> guild;
    private Role defaultRole = Role.BLOCKED;
}
