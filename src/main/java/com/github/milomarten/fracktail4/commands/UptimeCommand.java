package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail.core.FracktailVersion;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.Getter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
@Getter
public class UptimeCommand implements SlashCommandWrapper, ApplicationListener<ApplicationReadyEvent> {
    private Instant startTime = null;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startTime = Instant.now();
    }

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("uptime")
                .description("Get uptime and other pertinent bot info.")
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        return Responses.reply(String.format("""
                Hi! My name is Fracktail. Milo Marten created me!
                You can see my source code here: https://github.com/milomarten/fracktail4/. You'll \
                notice that I'm on version %s.
                I started up <t:%d:R>. Wow!
                """, FracktailVersion.getVersion(), startTime == null ? 0 : startTime.getEpochSecond()))
                .respond(event);
    }
}
