package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

@UtilityClass
public class Responses {
    public SlashCommandResponse reply(String words) {
        return event -> event.reply(words);
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
}
