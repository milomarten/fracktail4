package com.github.milomarten.fracktail4.commands.remind;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.milomarten.fracktail.core.persistence.Persistence;
import com.github.milomarten.fracktail.core.persistence.PersistenceBean;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.MessageChannel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class RemindHandler implements PersistenceBean {
    private Map<Snowflake, List<ReminderJob>> reminderMap = new HashMap<>();
    private final Persistence persistence;
    private final TaskScheduler registrar;
    private final GatewayDiscordClient client;

    @PostConstruct
    public void setUp() {
        var now = Instant.now();
        load().block();
        var passedJobs = new ArrayList<Runnable>();
        if (reminderMap == null) { reminderMap = new HashMap<>(); }
        reminderMap.forEach((key, jobs) -> jobs.forEach(rj -> {
            if (rj.passed(now)) {
                passedJobs.add(() -> runJob(key, rj, true));
            } else {
                scheduleJobNoStore(key, rj);
            }
        }));
        passedJobs.forEach(Runnable::run);
        log.info("Reinstated {} reminders", reminderMap.values().stream().mapToInt(List::size).sum() - passedJobs.size());

        if (!passedJobs.isEmpty()) {
            log.info("Triggered {} late reminders", passedJobs.size());
        }
    }

    public void runJob(Snowflake userId, ReminderJob job, boolean late) {
        if (jobStillExists(userId, job)) {
            log.info("Running job: {}", job.content());
            client.getUserById(userId)
                    .zipWhen(u -> job.where() == null ? u.getPrivateChannel() : client.getChannelById(job.where()).cast(MessageChannel.class))
                    .flatMap(tuple -> {
                        var user = tuple.getT1();
                        var channel = tuple.getT2();

                        var message = String.format("%s, I have a reminder for you! The message is: %s", user.getMention(), job.content());
                        if (late) {
                            message = "Sorry, I was under maintenance, and couldn't deliver your message when you needed it. But I'm here now...\n" + message;
                        }
                        return channel.createMessage(message);
                    })
                    .onErrorResume(e -> {
                        log.error("Error executing job", e);
                        return Mono.empty();
                    })
                    .then(removeJob(userId, job))
                    .onErrorResume(e -> {
                        log.error("Error removing job", e);
                        return Mono.empty();
                    })
                    .subscribe();
        } else {
            log.info("Went to run job that was cancelled. Nothing happened.");
        }
    }

    public Mono<Void> scheduleJob(Snowflake userId, ReminderJob job) {
        registrar.schedule(() -> runJob(userId, job, false), job);
        reminderMap.computeIfAbsent(userId, id -> new ArrayList<>()).add(job);
        return store();
    }

    private void scheduleJobNoStore(Snowflake userId, ReminderJob job) {
        registrar.schedule(() -> runJob(userId, job, false), job);
    }

    public Mono<Void> removeJob(Snowflake userId, ReminderJob rj) {
        reminderMap.getOrDefault(userId, List.of()).remove(rj);
        if (numberOfJobsForUser(userId) == 0) {
            // Cleanup to avoid any empty arrays in persistence. Takes up unnecessary space!!
            reminderMap.remove(userId);
        }
        return store();
    }

    public boolean jobStillExists(Snowflake userId, ReminderJob rj) {
        return reminderMap.get(userId).contains(rj);
    }

    public int numberOfJobsForUser(Snowflake userId) {
        if (reminderMap.containsKey(userId)) {
            return reminderMap.get(userId).size();
        }
        return 0;
    }

    @Override
    public Mono<Void> load() {
        return persistence.retrieve("remind-me", new TypeReference<Map<Snowflake, List<ReminderJob>>>() {
                })
                .doOnSuccess(m -> this.reminderMap = m)
                .then();
    }

    @Override
    public Mono<Void> store() {
        return persistence.store("remind-me", reminderMap);
    }
}
