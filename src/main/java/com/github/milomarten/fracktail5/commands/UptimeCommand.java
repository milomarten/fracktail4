package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.FracktailVersion;
import com.github.milomarten.fracktail.core.discord.DiscordTimestampFormat;
import com.github.milomarten.fracktail5.platform.discord.DiscordNoArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.Getter;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Getter
public class UptimeCommand implements DiscordSlashCommand, ApplicationListener<ApplicationReadyEvent> {
    private Instant startTime = null;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startTime = Instant.now();
    }

    private final Details DISCORD_SPEC = new DiscordNoArgumentDetails(
            "uptime",
            "Get uptime and other pertinent bot info.",
            () -> {
                return DiscordResponses.reply(String.format("""
                        Hi! My name is Fracktail. Milo Marten created me!
                        You can see my source code here: https://github.com/milomarten/fracktail4/. You'll \
                        notice that I'm on version %s.
                        I started up %s. Wow!
                        """,
                        FracktailVersion.getVersion(),
                        startTime == null ? "???" : DiscordTimestampFormat.RELATIVE.toDiscord(startTime)));
            });

    @Override
    public Details getDiscordSlashCommandDetails() {
        return DISCORD_SPEC;
    }
}
