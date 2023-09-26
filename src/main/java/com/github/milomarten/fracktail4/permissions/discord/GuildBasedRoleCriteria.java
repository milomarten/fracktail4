package com.github.milomarten.fracktail4.permissions.discord;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GuildBasedRoleCriteria extends RoleCriteria {
    private Long guildId;
}
