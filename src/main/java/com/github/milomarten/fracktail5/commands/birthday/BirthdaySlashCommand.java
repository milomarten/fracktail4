package com.github.milomarten.fracktail5.commands.birthday;

import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BirthdaySlashCommand implements DiscordSlashCommand {
    private final BirthdaySetSubCommand birthdaySetSubCommand;
    private final BirthdayNextSubCommand birthdayNextSubCommand;
    private final BirthdayPreviousSubCommand birthdayPreviousSubCommand;

    @Override
    public Details getDiscordSlashCommandDetails() {
        return new DiscordSubCommand(
            "birthday",
            "Get information about your fellow member's birthdays!"
            )
                    .addBranch(birthdaySetSubCommand.getDetails())
                    .addBranch(birthdayNextSubCommand.getDetails())
                    .addBranch(birthdayPreviousSubCommand.getDetails());
    }
}
