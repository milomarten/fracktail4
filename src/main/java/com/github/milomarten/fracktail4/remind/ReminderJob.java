package com.github.milomarten.fracktail4.remind;

import discord4j.common.util.Snowflake;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;

import java.time.Instant;

public record ReminderJob(Instant when, Snowflake where, String content, boolean ephemeral) implements Trigger {
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
}
