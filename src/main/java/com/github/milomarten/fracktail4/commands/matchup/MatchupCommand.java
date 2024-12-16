package com.github.milomarten.fracktail4.commands.matchup;

import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import org.springframework.stereotype.Component;

@Component
public class MatchupCommand extends AbstractSlashCommand<MatchupParams> {
    @Override
    public Class<MatchupParams> getParameterClass() {
        return MatchupParams.class;
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder) {
        return builder.name("matchup")
                .description("Lookup type matchups for your favorite RPG system");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, MatchupParams parameters) {
        return parameters.visit(this);
    }
}
