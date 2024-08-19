package com.github.milomarten.fracktail4.platform.discord.react;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * ID that indicates this ReactMessage is attached to another.
     * If the link is =/= -1, the link is equal to the ID of the ReactMessage following
     * this one.
     */
    private int link = -1;

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
        this.setLink(toCopy.getLink());
    }

    @JsonIgnore
    public boolean isLinked() {
        return this.link > -1;
    }

    public void unlink() {
        this.link = -1;
    }

    @JsonIgnore
    public boolean hasMoreThan20Options() {
        return options.size() > 20;
    }

    @JsonIgnore
    public boolean hasNoOptions() {
        return options.isEmpty();
    }

    public ReactMessage<ID> splitOffExcessChoices() {
        var newMessage = new ReactMessage<ID>();
        newMessage.guildId = this.guildId;
        newMessage.channelId = this.channelId;
        newMessage.description = "";

        var newMessageOptions = new ArrayList<ReactOption<ID>>();
        var oldMessageOptions = new ArrayList<ReactOption<ID>>();
        for (int i = 0; i < this.options.size(); i++) {
            if (i < 20) {
                oldMessageOptions.add(this.options.get(i));
            } else {
                newMessageOptions.add(this.options.get(i));
            }
        }
        this.options = oldMessageOptions;
        newMessage.options = newMessageOptions;

        return newMessage;
    }
}
