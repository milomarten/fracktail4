package com.github.milomarten.fracktail4.commands.amtrak.parameters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.milomarten.fracktail4.commands.amtrak.AmtrakCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AmtrakStationLookup.class, name = "station"),
        @JsonSubTypes.Type(value = AmtrakRouteLookup.class, name = "route"),
        @JsonSubTypes.Type(value = AmtrakTrainLookup.class, name = "train")
})
public interface AmtrakLookup {
    SlashCommandResponse doLookup(AmtrakCommand root);
}
