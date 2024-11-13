package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import jakarta.validation.ConstraintViolation;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

import java.util.Set;

@UtilityClass
public class Responses {
    public SlashCommandResponse reply(String words) {
        return event -> event.reply(words);
    }

    public SlashCommandResponse reply(String words, boolean ephemeral) {
        return ephemeral ? replyEphemeral(words) : reply(words);
    }

    public SlashCommandResponse replyEphemeral(String words) {
        return event -> event.reply().withEphemeral(true).withContent(words);
    }

    public SlashCommandResponse delayedReply(Mono<String> response) {
        return event -> event.deferReply()
                .then(Mono.defer(() -> response))
                .flatMap(event::createFollowup);
    }

    public SlashCommandResponse delayedReplyEphemeral(Mono<String> response) {
        return event -> event.deferReply()
                .withEphemeral(true)
                .then(Mono.defer(() -> response))
                .flatMap(event::createFollowup);
    }

    public <T> SlashCommandResponse replyValidation(Set<ConstraintViolation<T>> violations) {
        return reply(violations.iterator().next().getMessage());
    }
}
