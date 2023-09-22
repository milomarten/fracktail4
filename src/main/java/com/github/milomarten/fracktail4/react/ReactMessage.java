package com.github.milomarten.fracktail4.react;

import discord4j.common.util.Snowflake;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ReactMessage<ID> {
    private Snowflake guildId;
    private Snowflake channelId;
    private Snowflake messageId;
    private String description;
    private List<ReactOption<ID>> options = new ArrayList<>();

    public ReactMessage() {
    }

    public ReactMessage(ReactMessage<ID> toCopy) {
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
