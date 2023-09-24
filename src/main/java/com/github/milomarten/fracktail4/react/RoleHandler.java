package com.github.milomarten.fracktail4.react;

import com.github.milomarten.fracktail4.react.roles.RoleReactMessage;
import com.github.milomarten.fracktail4.base.platform.DiscordHookSource;
import com.github.milomarten.fracktail4.persistence.Persistence;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageEditSpec;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

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
