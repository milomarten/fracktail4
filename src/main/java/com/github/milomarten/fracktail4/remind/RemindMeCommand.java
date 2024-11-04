package com.github.milomarten.fracktail4.remind;

import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemindMeCommand implements SlashCommandWrapper {
    private static final Duration MINIMUM_DURATION = Duration.ofMinutes(5);

    private final RemindHandler handler;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("remind-me")
                .description("Set a reminder to do something.")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("when")
                        .description("When the reminder should fire. ex: 1h30m")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("message")
                        .description("What the reminder should say")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("where")
                        .description("Where the reminder should be. Default: DM")
                        .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                        .required(false)
                        .build()
                )
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var now = Instant.now();

        var userId = event.getInteraction().getUser().getId();
        if (hasTooManyReminders(userId)) {
            return SlashCommands.replyEphemeral(event, "You have more than three active reminders already. Can't add anymore.");
        }

        Duration d;
        try {
            var raw = event.getOption("when")
                    .flatMap(a -> a.getValue())
                    .map(a -> a.asString())
                    .orElseThrow();
            d = DurationUtils.stringToDuration(raw);
            if (checkDurationTooSmall(d)) {
                return SlashCommands.replyEphemeral(event, "`when` should be 5 minutes or more.");
            }
        } catch (Exception e) {
            return SlashCommands.replyEphemeral(event, "`when` is invalid. Should be like `1h30m`");
        }
        var message = event.getOption("message")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString())
                .orElseThrow();
        var where = event.getOption("where")
                .flatMap(a -> a.getValue())
                .map(a -> a.asSnowflake())
                .orElse(null);

        var job = new ReminderJob(now.plus(d), where, message);
        return event.deferReply()
                .then(handler.scheduleJob(userId, job).thenReturn(true))
                .flatMap(b -> SlashCommands.followup(event, "Sure! I'll remind you in " + DurationUtils.durationToString(d) + "(" + job.toDiscordTimestamp() + ")"));
    }

    private boolean checkDurationTooSmall(Duration d) {
        return d.compareTo(MINIMUM_DURATION) < 0;
    }

    private boolean hasTooManyReminders(Snowflake userId) {
        return handler.numberOfJobsForUser(userId) >= 3;
    }
}
