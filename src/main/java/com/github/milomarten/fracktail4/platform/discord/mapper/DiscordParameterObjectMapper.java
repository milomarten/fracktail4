package com.github.milomarten.fracktail4.platform.discord.mapper;

import discord4j.core.object.command.ApplicationCommandInteractionOption;

import java.util.List;

public interface DiscordParameterObjectMapper<PARAM> {
    List<ApplicationCommandInteractionOption> toDiscordParameters(PARAM pojo);
    PARAM toParameterObject(List<ApplicationCommandInteractionOption> discordParams);
}
