package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.platform.discord.mapper.DiscordObjectMapper;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Component
public class TestCommand implements SlashCommandWrapper {
    private final DiscordObjectMapper dom;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("test")
                .description("test")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("param")
                        .description("Parameter")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                )
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var obj = dom.map(event);
        System.out.println(obj);
        return event.reply("Sure!").withEphemeral(true);
    }
}
