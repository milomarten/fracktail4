package com.github.milomarten.fracktail4.remind;

import com.github.milomarten.fracktail4.persistence.Persistence;
import com.github.milomarten.fracktail4.persistence.PersistingObject;
import discord4j.common.util.Snowflake;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


@Configuration
public class RemindConfig {
    public PersistingObject<Map<Snowflake, ReminderJob>> reminderMap(Persistence persistence) {
        return new PersistingObject<>(persistence, "remind-me") {
            @Override
            protected Map<Snowflake, ReminderJob> createInitial() {
                return new HashMap<>();
            }
        };
    }
}
