package com.github.milomarten.fracktail4.permissions.discord;

import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.permissions.Role;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class RoleEngine {
    private final RoleConfiguration roleConfiguration;

    public boolean canUseCommand(Member caller, Command command) {
        var data = command.getCommandData();
        return getBestRole(caller).meetsOrExceeds(data.getRole());
    }

    public boolean canUseCommand(User user, Command command) {
        var data = command.getCommandData();
        return getBestRole(user).meetsOrExceeds(data.getRole());
    }

    private Role getBestRole(Member caller) {
        return roleConfiguration.getGuild()
                .stream()
                .filter(gbrc -> gbrc.getGuildId().equals(caller.getGuildId().asLong()))
                .findFirst()
                .map(gbrc -> {
                    return gbrc.getMappings()
                            .stream()
                            .mapMulti((RoleMapping mapping, Consumer<Role> acc) -> {
                                if (mapping.matches(caller)) {
                                    acc.accept(mapping.getRole());
                                }
                            })
                            .min(Comparator.comparing(Enum::ordinal))
                            .orElseGet(gbrc::getDefaultRole);
                })
                .orElse(roleConfiguration.getDefaultRole());
    }

    private Role getBestRole(User user) {
        return roleConfiguration.getDm()
                .getMappings()
                .stream()
                .filter(rm -> rm.matches(user))
                .findFirst()
                .map(RoleMapping::getRole)
                .orElse(roleConfiguration.getDefaultRole());
    }
}
