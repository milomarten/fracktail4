package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail5.platform.discord.DiscordNoArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import org.springframework.stereotype.Component;

@Component
public class MathCommand implements DiscordSlashCommand {
    private static final DiscordNoArgumentDetails SPEC = new DiscordNoArgumentDetails(
            "math",
            "Perform spectacular feats of math!",
            () -> DiscordResponses.reply("The answer is three.")
    );

    @Override
    public Details getDiscordSlashCommandDetails() {
        return SPEC;
    }
}
