package com.github.milomarten.fracktail4.react.roles;

import com.github.milomarten.fracktail4.react.AbstractReactHandler;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleHandler extends AbstractReactHandler<Snowflake> {
    private final Persistence persistence;

    @PostConstruct
    public void load() {
        var reacts = persistence.retrieve("role-reacts", RoleReactMessage[].class)
                .block();
        log.info("Loaded {} existing role reacts from persistence", reacts == null ? 0 : reacts.length);
        if (reacts != null) {
            this.getRoleReactMessages().addAll(List.of(reacts));
        }
    }

    @Override
    protected Mono<Void> onReact(Member member, Snowflake snowflake) {
        return member.addRole(snowflake, "Self-assign");
    }

    @Override
    protected Mono<Void> onUnreact(Member member, Snowflake snowflake) {
        return member.removeRole(snowflake, "Self-unassign");
    }

    @Override
    protected Mono<Void> updatePersistence() {
        return persistence.store("role-reacts", this.getRoleReactMessages());
    }
}
