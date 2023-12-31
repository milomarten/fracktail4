package com.github.milomarten.fracktail4.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.base.CommandData;
import com.github.milomarten.fracktail4.base.CommandFlow;
import com.github.milomarten.fracktail4.base.CommandParam;
import com.github.milomarten.fracktail4.base.parameter.Parameters;
import com.github.milomarten.fracktail4.base.parameter.*;
import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import com.github.milomarten.fracktail4.permissions.Role;
import com.github.milomarten.fracktail4.react.ReactMessage;
import com.github.milomarten.fracktail4.react.ReactOption;
import com.github.milomarten.fracktail4.react.roles.RoleHandler;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.OptionalInt;

@RequiredArgsConstructor
@Component
public class RoleReactCommand implements DiscordCommand {
    private static final ParameterParser PARSER = SubcommandParameterParser.builder()
            .option("set-description", NoOpParameterParser.INSTANCE)
            .build();
    private final RoleHandler handler;

    private final ObjectMapper objectMapper;

    private ReactMessage<Snowflake> oven = null;

    @Override
    public CommandData getCommandData() {
        return CommandData.builder()
                .id("role-react")
                .alias("role-react")
                .description("Handle role react commands")
                .flow(CommandFlow.of("Initialize a new Role React message", "create"))
                .flow(CommandFlow.of("Discard all changes to the current Role React", "discard"))
                .flow(CommandFlow.of("Delete the Role React message completely", "delete"))
                .flow(CommandFlow.of("Edit a Role React message. Requires publish to make changes take effect", "edit", "role-react-idx"))
                .flow(CommandFlow.of("Dump the raw contents of the Role React message so far.", "publish"))
                .flow(CommandFlow.of("Set the Discord channel this message should be put in.", "set-channel", "channel-id"))
                .flow(CommandFlow.of("Set the text describing the role react message.", "set-description", "description-text"))
                .flow(CommandFlow.of("Publishes the Role React message in the specified location", "publish"))
                .flow(CommandFlow.of("Adds a choice to the Role React", "choice-add", "emoji", "role-id"))
                .flow(CommandFlow.of("Remove a choice from the Role React", "choice-remove", "emoji"))
                .build();
    }

    @Override
    public Role getRequiredRole() {
        return Role.MODERATOR;
    }

    @Override
    public ParameterParser getParameterParser() {
        return PARSER;
    }

    @Override
    public Mono<?> doCommand(Parameters parameters, MessageCreateEvent event) {
        Optional<String> first = parameters.getParameter(0);
        if (first.isEmpty()) {
            return respondWithDM(event, "Missing initial parameter. Should be: ");
        }
        return switch (first.get()) {
            case "create" -> create(event);
            case "discard" -> discard(event);
            case "delete" -> delete(event, parameters.range(1));
            case "edit" -> edit(event, parameters.range(1));
            case "preview" -> preview(event);
            case "set-channel" -> setChannel(event, parameters.range(1));
            case "set-description" -> setDescription(event, parameters.range(1));
            case "publish" -> publish(event);
            case "choice-add" -> addChoice(event, parameters.range(1));
            case "choice-remove" -> removeChoice(event, parameters.range(1));
            default -> respondWithDM(event, "Unknown command " + first.get() + ". Should be: ");
        };
    }

    private Mono<?> nothingInProgress(MessageCreateEvent event) {
        return failure(event, "There is no role react in progress. Use `!role-react create` to create one.");
    }

    private Mono<Void> success(MessageCreateEvent event) {
        return reactWith(event, "☑️");
    }

    private Mono<Void> failure(MessageCreateEvent event, String cause) {
        return reactWith(event, "❌")
                .then(respondWithDM(event, cause))
                .then();
    }

    private Mono<?> create(MessageCreateEvent event) {
        if (oven != null) {
            return failure(event, "There is a role react in progress. Use `!role-react discard` to discard.");
        }

        this.oven = new ReactMessage<>();
        oven.setGuildId(event.getGuildId().orElse(null));
        oven.setChannelId(event.getMessage().getChannelId());

        return success(event);
    }

    private Mono<?> setChannel(MessageCreateEvent event, Parameters parameters) {
        if (oven == null) { return nothingInProgress(event); }
        Optional<Snowflake> channelId = parameters.getSnowflake(0);
        if (channelId.isEmpty()) {
            return respondWithDM(event, "Correct use is `!role-react set-channel <snowflake>`");
        }

        return event.getClient()
                .getChannelById(channelId.get())
                .flatMap(channel -> {
                    if (channel instanceof TextChannel tc) {
                        this.oven.setGuildId(tc.getGuildId());
                        this.oven.setChannelId(tc.getId());
                        return success(event);
                    } else {
                        return failure(event, "Channel must be a normal text channel.");
                    }
                })
                .onErrorResume(ex -> failure(event,
                        "I ran into an issue getting the channel information. Sorry. Exception: " + ex.getMessage()));
    }

    private Mono<?> setDescription(MessageCreateEvent event, Parameters params) {
        if (oven == null) { return nothingInProgress(event); }
        String content = params.getParameter(0).orElse("");

        this.oven.setDescription(content);

        return success(event);
    }

    public Mono<?> addChoice(MessageCreateEvent event, Parameters parameters) {
        if (oven == null) { return nothingInProgress(event); }
        var emojiMaybe = parameters.getEmoji(0);
        var choiceMaybe = parameters.getSnowflake(1)
                .or(() -> parameters.getRoleMention(1));

        if (emojiMaybe.isEmpty() || choiceMaybe.isEmpty()) {
            return failure(event, "Correct use is `!role-react choice-add <emoji> <role-id>`");
        }

        var emoji = emojiMaybe.get();
        var choice = choiceMaybe.get();

        if (oven.getOptions().stream().anyMatch(re -> re.getEmoji().equals(emoji))) {
            var formattedEmoji = emoji.asCustomEmoji()
                    .map(ReactionEmoji.Custom::asFormat)
                    .or(() -> emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw))
                    .orElse("???");
            return failure(event, "Emoji " + formattedEmoji + " is already used as a choice. Please use another.");
        }

        return event.getClient()
                .getRoleById(oven.getGuildId(), choice)
                .flatMap(role -> {
                    ReactOption<Snowflake> option = new ReactOption<>();
                    option.setEmoji(emoji);
                    option.setDescription(role.getName());
                    option.setId(choice);
                    oven.getOptions()
                            .add(option);
                    return success(event);
                })
                .onErrorResume(ex -> failure(event,
                        "I ran into an issue getting the role information. Sorry. Exception: " + ex.getMessage()));
    }

    public Mono<?> removeChoice(MessageCreateEvent event, Parameters parameters) {
        if (oven == null) { return nothingInProgress(event); }
        var emojiMaybe = parameters.getEmoji(0);

        if (emojiMaybe.isPresent()) {
            var emoji = emojiMaybe.get();
            var removed = oven.getOptions()
                    .removeIf(ro -> emoji.equals(ro.getEmoji()));
            if (removed) {
                return success(event);
            } else {
                String formattedEmoji = emoji.asCustomEmoji().map(ReactionEmoji.Custom::asFormat)
                        .or(() -> emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw))
                        .orElse("???");
                return failure(event, "Emoji " + formattedEmoji + " is not present in the list. Nothing happened.");
            }
        } else {
            return failure(event, "Correct use is `!role-react choice-delete <emoji>`");
        }
    }

    private Mono<?> discard(MessageCreateEvent event) {
        oven = null;
        return success(event);
    }

    private Mono<?> publish(MessageCreateEvent event) {
        if (oven == null) { return nothingInProgress(event); }

        return this.handler.publish(this.oven)
                .flatMap(idx -> respondWithDM(event, "Role React has been published. ID is: " + idx))
                .doOnSuccess(msg -> this.oven = null)
                .then()
                .onErrorResume(ex -> failure(event, "I ran into an issue publishing your Role React. Sorry. Exception: " + ex.getMessage()));
    }

    public Mono<?> preview(MessageCreateEvent event) {
        if (oven == null) { return nothingInProgress(event); }

        try {
            String block = String.format("```json\n%s\n```", objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this.oven));
            return respondWithDM(event, block);
        } catch (Exception ex) {
            return failure(event, "I ran into an issue rendering the preview. Sorry. Exception: " + ex.getMessage());
        }
    }

    private Mono<?> edit(MessageCreateEvent event, Parameters parameters) {
        OptionalInt maybeId = parameters.getIntParameter(0);

        if (maybeId.isEmpty()) {
            return failure(event, "Correct use is `role-react edit <id>`");
        }

        var messageMaybe = this.handler.getById(maybeId.getAsInt());
        if (messageMaybe.isEmpty()) {
            return failure(event, "No Role React found with ID " + maybeId.getAsInt());
        }

        this.oven = new ReactMessage<>(messageMaybe.get());
        return success(event);
    }

    private Mono<?> delete(MessageCreateEvent event, Parameters parameters) {
        OptionalInt maybeId = parameters.getIntParameter(0);

        if (maybeId.isEmpty()) {
            return failure(event, "Correct use is `role-react delete <id>`");
        }

        return this.handler.deleteById(maybeId.getAsInt())
                .switchIfEmpty(success(event))
                .onErrorResume(ex -> failure(event, "I ran into an issue deleting your Role React. Sorry. Exception: " + ex.getMessage()).then());
    }
}
