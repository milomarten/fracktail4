package com.github.milomarten.fracktail5.commands.amtrak;

import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AmtrakSlashCommand implements DiscordSlashCommand {
    private final AmtrakLookupTrainSubCommand amtrakLookupTrainSubCommand;

    @Override
    public Details getDiscordSlashCommandDetails() {
        return new DiscordSubCommand("amtrak", "Retrieve Amtrak information")
                .addBranch(amtrakLookupTrainSubCommand.getDetails());
    }
}
