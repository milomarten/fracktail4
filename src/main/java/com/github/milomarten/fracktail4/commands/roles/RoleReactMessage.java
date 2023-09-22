package com.github.milomarten.fracktail4.commands.roles;

import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RoleReactMessage extends ReactMessage<Snowflake> {
    public RoleReactMessage() {
    }

    public RoleReactMessage(RoleReactMessage toCopy) {
        this.setGuildId(toCopy.getGuildId());
        this.setChannelId(toCopy.getChannelId());
        this.setMessageId(toCopy.getMessageId());
        this.setDescription(toCopy.getDescription());
        this.setOptions(toCopy.getOptions().stream()
                .map(ReactOption::new)
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }
}
