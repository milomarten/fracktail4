package com.github.milomarten.fracktail4.commands.matchup;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SCARMatchupParams.class, name = "scar")
})
public interface MatchupParams {
    SlashCommandResponse visit(MatchupCommand matchupCommand);
}
