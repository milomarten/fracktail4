package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import com.github.milomarten.fracktail4.base.CommandOutputFinalizer;
import com.github.milomarten.fracktail4.platform.discord.utils.DiscordLocaleMapper;
import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DiscordCommandOutputFinalizer implements CommandOutputFinalizer<ApplicationCommandInteractionEvent, String> {
    private final DiscordLocaleMapper mapper;
    private final MessageSource translations;

    @Override
    public String mapResponse(ApplicationCommandInteractionEvent event, String response) {
        return translations.getMessage(response, null, mapper.getLocale(event));
    }
}
