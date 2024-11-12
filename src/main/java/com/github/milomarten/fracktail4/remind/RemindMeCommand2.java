package com.github.milomarten.fracktail4.remind;

import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordJacksonMapper;
import com.github.milomarten.fracktail4.platform.discord.mapper.ObjectToDiscordReflectiveMapper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SimpleDiscordParameterCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Set;

//@Component
public class RemindMeCommand2 extends SimpleDiscordParameterCommand<RemindMeParameters> {
    public RemindMeCommand2(DiscordJacksonMapper discordJacksonMapper, ObjectToDiscordReflectiveMapper objectToDiscordReflectiveMapper, Validator validator) {
        super(RemindMeParameters.class, discordJacksonMapper, objectToDiscordReflectiveMapper, validator);
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augmentRequest(ImmutableApplicationCommandRequest.Builder builder) {
        return builder
                .name("remind-me")
                .description("Set a reminder to do something.");
    }

    @Override
    protected SlashCommandResponse handleEvent(RemindMeParameters remindMeParameters) {
        return Responses.reply("Hi!");
    }

    @Override
    protected SlashCommandResponse handleConstraintViolations(Set<ConstraintViolation<RemindMeParameters>> constraintViolations) {
        return Responses.reply(constraintViolations.iterator().next().getMessage());
    }
}
