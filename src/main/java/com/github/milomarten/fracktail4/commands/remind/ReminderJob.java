package com.github.milomarten.fracktail4.commands.remind;

import discord4j.common.util.Snowflake;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Instant;

public record ReminderJob(Instant when, Snowflake where, String content) implements Trigger {
    @Override
    public Instant nextExecution(TriggerContext triggerContext) {
        // These jobs should happen once and only once. Thus, they should return null
        // as soon as the job has run once.
        if (triggerContext.lastCompletion() == null) {
            return this.when;
        } else {
            return null;
        }
    }

    public String toDiscordTimestamp() {
        return String.format("<t:%d:f>", when.getEpochSecond());
    }

    public boolean passed(Instant now) {
        return now.equals(when) || now.isAfter(when);
    }
}
