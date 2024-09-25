package com.github.milomarten.fracktail4.platform.discord.utils;

import discord4j.core.event.domain.interaction.ApplicationCommandInteractionEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class DiscordLocaleMapper {
    public Locale getLocale(ApplicationCommandInteractionEvent event) {
        var locale = event.getInteraction().getUserLocale();
        return Locale.forLanguageTag(locale);
    }
}
