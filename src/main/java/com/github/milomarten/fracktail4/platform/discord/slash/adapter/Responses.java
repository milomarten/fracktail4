package com.github.milomarten.fracktail4.platform.discord.slash.adapter;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.function.Function;

/**
 * Standard sets of common Discord bot responses
 */
@UtilityClass
@Slf4j
public class Responses {
    /**
     * Immediately and publicly reply to the command with some text.
     * @param words The text to respond with
     * @return A SlashCommandResponse that says those words.
     */
    public SlashCommandResponse reply(String words) {
        return event -> event.reply(words);
    }

    /**
     * Immediately reply to the command, but only so the user sees it.
     * @param words The text to respond with
     * @return A SlashCommandResponse that says those words.
     */
    public SlashCommandResponse replyEphemeral(String words) {
        return event -> event.reply().withEphemeral(true).withContent(words);
    }

    /**
     * Immediately reply to the command, either publicly or just to the user
     * @param words The text to respond with
     * @param ephemeral True, if the text should only be visible to the user
     * @return A SlashCommandResponse that says those words.
     */
    public SlashCommandResponse reply(String words, boolean ephemeral) {
        return ephemeral ? replyEphemeral(words) : reply(words);
    }

    /**
     * Reply publicly to a command after performing some slow logic.
     * For commands that may exceed 3 seconds, the bot is responsible for "deferring" the command,
     * for follow up later. API calls, for instance, should use this.
     * @param response The eventual response to reply with
     * @return A SlashCommandResponse that says those words eventually
     */
    public SlashCommandResponse delayedReply(Mono<String> response) {
        return event -> event.deferReply()
                .then(response)
                .flatMap(event::createFollowup);
    }

    /**
     * Reply to a command after performing some slow logic, but only to the user.
     * For commands that may exceed 3 seconds, the bot is responsible for "deferring" the command,
     * for follow up later. API calls, for instance, should use this.
     * @param response The eventual response to reply with
     * @return A SlashCommandResponse that says those words eventually
     */
    public SlashCommandResponse delayedReplyEphemeral(Mono<String> response) {
        return event -> event.deferReply()
                .withEphemeral(true)
                .then(response)
                .flatMap(event::createFollowup);
    }

    /**
     * Perform some response logic, using the locale of the user.
     * Useful for localization messages.
     * @param func A function which returns the SlashCommandResponse to perform for a given locale.
     * @return A SlashCommandResponse which extracts the user's locale and passes it on.
     */
    public SlashCommandResponse withLocale(Function<Locale, SlashCommandResponse> func) {
        return event -> {
            return func.apply(fromDiscordLocale(event.getInteraction().getUserLocale()))
                    .respond(event);
        };
    }

    /**
     * Perform some response logic, using the identity of the user.
     * In some cases, you will need to perform different logic if the user is a member
     * or not, while retaining mostly the same logic beyond that. For instance, a command that greets
     * the user by their Username in DMs, but by their nickname in a guild. This could be done simply with:
     * <code>
     *     withUserOrMember(
     *                 User::getGlobalName,
     *                 Member::getNickname,
     *                 name -> Responses.reply("Hello, " + name + "!")
     *         );
     * </code>
     * @param userFunc The function to map a raw user into some object
     * @param memberFunc The function to map a member into some object
     * @param combiner The common logic that creates the final response
     * @return A SlashCommandResponse that enacts user/member based logic
     * @param <T> A common type between User and Member objects to do final work on.
     */
    public <T> SlashCommandResponse withUserOrMember(
            Function<User, T> userFunc,
            Function<Member, T> memberFunc,
            Function<T, SlashCommandResponse> combiner) {
        return event -> {
            var t = event.getInteraction().getMember()
                    .map(memberFunc)
                    .orElseGet(() -> userFunc.apply(event.getInteraction().getUser()));
            return combiner.apply(t).respond(event);
        };
    }

    private Locale fromDiscordLocale(String tag) {
        return Locale.forLanguageTag(tag);
    }
}
