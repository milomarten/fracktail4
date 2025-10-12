package com.github.milomarten.fracktail5.commands;

import com.github.milomarten.fracktail.core.remind.DurationUtils;
import com.github.milomarten.fracktail.core.remind.RemindHandler;
import com.github.milomarten.fracktail.core.remind.ReminderJob;
import com.github.milomarten.fracktail5.platform.discord.DiscordArgumentDetails;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.DiscordSlashCommand;
import com.github.milomarten.fracktail5.platform.discord.argument.ContextParameter;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RemindMeCommand implements DiscordSlashCommand {
    private static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);
    private static final Duration MAXIMUM_DURATION = Duration.ofDays(60);

    private final RemindHandler handler;

    @Override
    public Details getDiscordSlashCommandDetails() {
        return new DiscordArgumentDetails<>(
                "remind-me",
                "Set a reminder to do something.",
                new PojoParser<>(Parameters::new)
                        .addField(
                                new ContextParameter<>(ChatInputInteractionEvent::getUser),
                                Parameters::setWho
                        )
                        .addField(
                                new StringArgumentParser(
                                        "when",
                                        "When the reminder should fire. ex: 1h30m"
                                ).required(),
                                Parameters::setWhen
                        )
                        .addField(
                                new StringArgumentParser(
                                        "message",
                                        "What the reminder should say"
                                ).required(),
                                Parameters::setMessage
                        )
                        .addField(
                                new ContextParameter<>(e -> e.getInteraction().getChannelId()),
                                Parameters::setWhere
                        ),
                this::remind
        );
    }

    private DiscordResponse remind(Parameters parameters) {
        var now = Instant.now();

        if (hasTooManyReminders(parameters.who.getId())) {
            return DiscordResponses.replyEphemeral("You have more than three active reminders already. Can't add anymore.");
        }

        if (!DurationUtils.isValidFormat(parameters.getWhen())) {
            return DiscordResponses.replyEphemeral("`when` is in an invalid format.");
        }

        var d = DurationUtils.stringToDuration(parameters.getWhen());
        if (!isDurationWithinWindow(d)) {
            return DiscordResponses.replyEphemeral("`when` can be no less than 5 minutes and no more than 60 days out.");
        }

        var job = new ReminderJob(now.plus(d), parameters.getWhere(), parameters.getMessage());
        return DiscordResponses.defer(
                handler.scheduleJob(parameters.getWho().getId(), job)
                        .thenReturn(true)
                        .thenReturn(DiscordResponses.replyEphemeral("Sure! I'll remind you in " + DurationUtils.durationToString(d) + "(" + job.toDiscordTimestamp() + ")"))
        );
    }

    private boolean hasTooManyReminders(Snowflake userId) {
        return handler.numberOfJobsForUser(userId) >= 3;
    }

    private boolean isDurationWithinWindow(Duration d) {
        return d.compareTo(MINIMUM_DURATION) >= 0 && d.compareTo(MAXIMUM_DURATION) <= 0;
    }

    @Data
    private static class Parameters {
        private User who;
        private String when;
        private String message;
        private Snowflake where;
    }
}
