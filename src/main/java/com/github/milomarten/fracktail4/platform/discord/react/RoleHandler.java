package com.github.milomarten.fracktail4.platform.discord.react;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Member;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoleHandler extends AbstractReactHandler<Snowflake> {
    private final Persistence persistence;

    private static final TypeReference<List<ReactMessage<Snowflake>>> ROLE_REACT_TYPE = new TypeReference<>() {  };

    @PostConstruct
    public void load() {
        var reacts = persistence.retrieve("role-reacts", ROLE_REACT_TYPE)
                .block();
        log.info("Loaded {} existing role reacts from persistence", reacts == null ? 0 : reacts.size());
        if (reacts != null) {
            this.getRoleReactMessages().addAll(reacts);
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
