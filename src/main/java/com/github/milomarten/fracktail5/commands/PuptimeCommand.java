package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.FracktailVersion;
import com.github.milomarten.fracktail4.commands.UptimeCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail5.platform.discord.DiscordNoArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.util.DiscordResponses;
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
public class PuptimeCommand implements DiscordSlashCommand, ApplicationListener<ApplicationReadyEvent> {
    private Instant startTime = null;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startTime = Instant.now();
    }

    private final Details DISCORD_SPEC = new DiscordNoArgumentDetails(
            "puptime",
            "Get uptime and other pertinent bot info?",
            () -> {
                return DiscordResponses.reply(String.format("""
                            Woof! My arf bark Fracktail. Milo Marten woofed me!
                            You bow-wow my arf yip bark: https://github.com/milomarten/fracktail4/. You'll \\
                            bark arf I'm sniff howl %s.
                            I woof woof <t:%d:R>. Bow-wow!
                        """,
                        FracktailVersion.getVersion(),
                        startTime == null ? 0 : startTime.getEpochSecond()));
            });

    @Override
    public Details getDiscordSlashCommandDetails() {
        return DISCORD_SPEC;
    }
}
