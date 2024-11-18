package com.github.milomarten.fracktail4.amtrak;

import com.github.milomarten.fracktail4.amtrak.parameters.AmtrakCommandParameters;
import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordParameterHelper;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Getter
@Component
@RequiredArgsConstructor
public class AmtrakCommand extends AbstractSlashCommand<AmtrakCommandParameters> {
    private final AmtrakLookup lookup;
    private final AmtrakGateway gateway;

    @Override
    public Class<AmtrakCommandParameters> getParameterClass() {
        return AmtrakCommandParameters.class;
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder) {
        return builder
                .name("amtrak")
                .description("Interact with the Amtrak API. Also supports VIA and Brightline!");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, AmtrakCommandParameters parameters) {
        return parameters.getLookup().doLookup(this);
    }
}
