package com.github.milomarten.fracktail4.commands.remind;

import com.github.milomarten.fracktail.core.remind.DurationUtils;
import com.github.milomarten.fracktail.core.remind.RemindHandler;
import com.github.milomarten.fracktail.core.remind.ReminderJob;
import com.github.milomarten.fracktail4.platform.discord.slash.AbstractSlashCommand;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ImmutableApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@Slf4j
@RequiredArgsConstructor
public class RemindMeCommand extends AbstractSlashCommand<RemindMeParameters> {
    private static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);
    private static final Duration MAXIMUM_DURATION = Duration.ofDays(60);

    private final RemindHandler handler;

    @Override
    public Class<RemindMeParameters> getParameterClass() {
        return RemindMeParameters.class;
    }

    @Override
    protected ImmutableApplicationCommandRequest.Builder augment(ImmutableApplicationCommandRequest.Builder builder) {
        return builder
                .name("remind-me")
                .description("Set a reminder to do something.");
    }

    @Override
    protected SlashCommandResponse handleEvent(ChatInputInteractionEvent event, RemindMeParameters parameters) {
        var now = Instant.now();

        var userId = event.getInteraction().getUser().getId();
        if (hasTooManyReminders(userId)) {
            return Responses.replyEphemeral("You have more than three active reminders already. Can't add anymore.");
        }

        var d = DurationUtils.stringToDuration(parameters.getWhen());
        if (!isDurationWithinWindow(d)) {
            return Responses.replyEphemeral("`when` can be no less than 5 minutes and no more than 60 days out.");
        }

        var job = new ReminderJob(now.plus(d), parameters.getWhere(), parameters.getMessage());
        return Responses.delayedReply(
                handler.scheduleJob(userId, job)
                        .thenReturn(true)
                        .thenReturn("Sure! I'll remind you in " + DurationUtils.durationToString(d) + "(" + job.toDiscordTimestamp() + ")")
        );
    }

    private boolean isDurationWithinWindow(Duration d) {
        return d.compareTo(MINIMUM_DURATION) >= 0 && d.compareTo(MAXIMUM_DURATION) <= 0;
    }

    private boolean hasTooManyReminders(Snowflake userId) {
        return handler.numberOfJobsForUser(userId) >= 3;
    }
}
