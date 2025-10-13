package com.github.milomarten.fracktail5.commands.birthday;

import com.github.milomarten.fracktail.core.birthday.BirthdayHandler;
import com.github.milomarten.fracktail.core.birthday.BirthdayUtils;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.DiscordUserCommand;
import com.github.milomarten.fracktail5.platform.discord.DiscordUserResponse;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordSubCommand;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BirthdaySlashCommand implements DiscordSlashCommand, DiscordUserCommand {
    private final BirthdaySetSubCommand birthdaySetSubCommand;
    private final BirthdayNextSubCommand birthdayNextSubCommand;
    private final BirthdayPreviousSubCommand birthdayPreviousSubCommand;
    private final BirthdayHandler handler;

    @Override
    public DiscordSlashCommand.Details getDiscordSlashCommandDetails() {
        return new DiscordSubCommand(
            "birthday",
            "Get information about your fellow member's birthdays!"
            )
                    .addBranch(birthdaySetSubCommand.getDetails())
                    .addBranch(birthdayNextSubCommand.getDetails())
                    .addBranch(birthdayPreviousSubCommand.getDetails());
    }

    @Override
    public DiscordUserCommand.Details getDiscordUserCommandDetails() {
        return new DiscordUserCommand.Details() {
            @Override
            public String getName() {
                return "birthday";
            }

            @Override
            public DiscordUserResponse invoke(UserInteractionEvent event) {
                var targetName = event.getResolvedUser().getGlobalName()
                        .orElseGet(() -> event.getResolvedUser().getUsername());
                var birthdayMaybe = handler.getBirthday(event.getTargetId());
                if (birthdayMaybe.isEmpty()) {
                    String fStr = String.format("I don't know %s' birthday, unfortunately", targetName);
                    return DiscordUserResponse.replyEphemeral(fStr);
                } else {
                    var birthday = birthdayMaybe.get();
                    String dayOf = BirthdayUtils.getDisplayBirthday(birthday.getDayOfCelebration());
                    String fStr = String.format("%s' birthday is on %s!", targetName, dayOf);
                    return DiscordUserResponse.replyEphemeral(fStr);
                }
            }
        };
    }
}
