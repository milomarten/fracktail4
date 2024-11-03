package com.github.milomarten.fracktail4.remind;

import discord4j.common.util.Snowflake;

import java.time.Instant;

public record ReminderJob(Instant when, Snowflake where, String content, boolean ephemeral) {
}
