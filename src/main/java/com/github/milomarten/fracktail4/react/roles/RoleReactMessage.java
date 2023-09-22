package com.github.milomarten.fracktail4.react.roles;

import com.github.milomarten.fracktail4.react.ReactMessage;
import com.github.milomarten.fracktail4.react.ReactOption;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class RoleReactMessage extends ReactMessage<Snowflake> {
    public RoleReactMessage() {
    }

    public RoleReactMessage(ReactMessage<Snowflake> toCopy) {
        super(toCopy);
    }
}
