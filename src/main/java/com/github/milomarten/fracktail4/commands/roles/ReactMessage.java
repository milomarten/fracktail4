package com.github.milomarten.fracktail4.commands.roles;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class ReactMessage<ID> {
    private Snowflake guildId;
    private Snowflake channelId;
    private Snowflake messageId;
    private String description;
    private List<ReactOption<ID>> options = new ArrayList<>();
}
