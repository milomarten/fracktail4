package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.FracktailVersion;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class PuptimeCommand implements SlashCommandWrapper {
    private final UptimeCommand cmd;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("puptime")
                .description("Get uptime and other pertinent bot info?")
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var startTime = cmd.getStartTime();
        return Responses.reply(String.format("""
                Woof! My arf bark Fracktail. Milo Marten woofed me!
                You bow-wow my arf yip bark: https://github.com/milomarten/fracktail4/. You'll \
                bark arf I'm sniff howl %s.
                I woof woof <t:%d:R>. Bow-wow!
                """, FracktailVersion.getVersion(), startTime == null ? 0 : startTime.getEpochSecond()))
                .respond(event);
    }
}
