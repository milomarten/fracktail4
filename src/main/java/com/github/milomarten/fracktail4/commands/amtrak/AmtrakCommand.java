package com.github.milomarten.fracktail4.commands.amtrak;

import com.github.milomarten.fracktail4.commands.amtrak.parameters.AmtrakCommandParameters;
import com.github.milomarten.fracktail.core.config.TemplateCache;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
public class AmtrakCommand extends AbstractSlashCommand<AmtrakCommandParameters> {
    private final AmtrakLookup lookup;
    private final AmtrakGateway gateway;
    private final TemplateCache templateCache;

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

    public String templateResponse(String name, Object context) {
        try {
            return templateCache.get("amtrak/" + name).apply(context);
        } catch (IOException e) {
            return "Error generating response, please contact the admin";
        }
    }
}
