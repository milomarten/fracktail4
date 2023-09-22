package com.github.milomarten.fracktail4.commands.roles;

import com.github.milomarten.fracktail4.base.Command;
import com.github.milomarten.fracktail4.base.CommandData;
import com.github.milomarten.fracktail4.base.Parameters;
import com.github.milomarten.fracktail4.base.platform.DiscordCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.OptionalInt;

@RequiredArgsConstructor
@Component
class RoleReactCommand implements Command, DiscordCommand {
    private final RoleHandler handler;

    @Value("${command.delimiter: }")
    private String delimiter;

    private RoleReactMessage oven = null;

    @Override
    public CommandData getCommandData() {
        return CommandData.builder()
                .id("role-react")
                .alias("role-react")
                .description("Handle role react commands")
                .param(CommandData.Param.builder()
                        .name("operation")
                        .build())
                .param(CommandData.Param.builder()
                        .name("args")
                        .build())
                .build();
    }

    @Override
    public Mono<?> doCommand(MessageCreateEvent event) {
        String[] tokens = event.getMessage().getContent().split(delimiter);
        Parameters params = new Parameters(tokens);
        Optional<String> first = params.getParameter(1);
        if (first.isEmpty()) {
            return respondWithDM(event, "Missing initial parameter. Should be: ");
        }
        var retVal = switch (first.get()) {
            case "create" -> create(event);
            case "discard" -> discard(event);
            case "delete" -> delete(event, params.range(2));
            case "edit" -> edit(event, params.range(2));
            case "set-channel" -> setChannel(event, params.range(2));
            case "set-description" -> setDescription(event);
            case "set-guild" -> setGuild(event, params.range(2));
            case "publish" -> publish(event);
            case "choice-add" -> addChoice(event, params.range(2));
            default -> respondWithDM(event, "Unknown command " + first.get() + ". Should be: ");
        };
        return retVal;
    }

    private Mono<?> create(MessageCreateEvent event) {
        if (oven != null) {
            return respondWithDM(event, "There is a role react in progress. Use `!role-react discard` to discard.");
        }

        this.oven = new RoleReactMessage();
        oven.setGuildId(event.getGuildId().orElse(null));
        oven.setChannelId(event.getMessage().getChannelId());

        return respondWithDM(event, "Created Role React Message. Continue setup and publish when done.");
    }

    private Mono<?> nothingInProgress(MessageCreateEvent event) {
        return respondWithDM(event, "There is no role react in progress. Use `!role-react create` to create one.");
    }

    public Mono<?> setGuild(MessageCreateEvent event, Parameters parameters) {
        if (oven == null) { return nothingInProgress(event); }
        Optional<Snowflake> guildId = parameters.getSnowflake(0);
        if (guildId.isEmpty()) {
            return respondWithDM(event, "Correct use is `!role-react set-guild <snowflake>`");
        }

        return event.getClient()
                .getGuildById(guildId.get())
                .flatMap(guild -> {
                    this.oven.setGuildId(guild.getId());
                    return respondWithDM(event, "Set guild to " + guild.getName());
                })
                .onErrorResume(ex -> respondWithDM(event,
                        "I ran into an issue getting the guild information. Sorry. Exception: " + ex.getMessage()));
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
                    if (channel.getType() == Channel.Type.GUILD_TEXT) {
                        this.oven.setChannelId(channel.getId());
                        return respondWithDM(event, "Set channel to " + ((TextChannel)channel).getName());
                    } else {
                        return respondWithDM(event, "Channel must be a normal text channel.");
                    }
                })
                .onErrorResume(ex -> respondWithDM(event,
                        "I ran into an issue getting the channel information. Sorry. Exception: " + ex.getMessage()));
    }

    private Mono<?> setDescription(MessageCreateEvent event) {
        if (oven == null) { return nothingInProgress(event); }
        String content = event.getMessage().getContent().replace("!role-react set-description", "");

        this.oven.setDescription(content);

        if (content.isBlank()) {
            return respondWithDM(event, "Cleared out description.");
        } else {
            return respondWithDM(event, "Set description.");
        }
    }

    public Mono<?> addChoice(MessageCreateEvent event, Parameters parameters) {
        if (oven == null) { return nothingInProgress(event); }
        var emojiMaybe = parameters.getEmoji(0);
        var choiceMaybe = parameters.getSnowflake(1)
                .or(() -> parameters.getRoleMention(1));

        if (emojiMaybe.isEmpty() || choiceMaybe.isEmpty()) {
            return respondWithDM(event, "Correct use is `!role-react choice-add <emoji> <role-id>`");
        }

        var emoji = emojiMaybe.get();
        var choice = choiceMaybe.get();

        if (oven.getOptions().stream().anyMatch(re -> re.getEmoji().equals(emoji))) {
            var formattedEmoji = emoji.asCustomEmoji()
                    .map(ReactionEmoji.Custom::asFormat)
                    .or(() -> emoji.asUnicodeEmoji().map(ReactionEmoji.Unicode::getRaw))
                    .orElse("???");
            return respondWithDM(event, "Emoji " + formattedEmoji + " is already used as a choice. Please use another.");
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
                    return respondWithDM(event, "Added react for role " + role.getName());
                })
                .onErrorResume(ex -> respondWithDM(event,
                        "I ran into an issue getting the role information. Sorry. Exception: " + ex.getMessage()));
    }

    private Mono<?> discard(MessageCreateEvent event) {
        oven = null;

        return respondWithDM(event, "Cancelled any Role React Messages being made.");
    }

    private Mono<?> publish(MessageCreateEvent event) {
        if (oven == null) { return nothingInProgress(event); }

        return this.handler.publish(this.oven)
                .flatMap(idx -> respondWithDM(event, "Role React has been published. ID is: " + idx))
                .doOnSuccess(msg -> this.oven = null)
                .doOnError(ex -> respondWithDM(event, "I ran into an issue publishing your Role React. Sorry. Exception: " + ex.getMessage()));
    }

    private Mono<?> edit(MessageCreateEvent event, Parameters parameters) {
        OptionalInt maybeId = parameters.getIntParameter(0);

        if (maybeId.isEmpty()) {
            return respondWithDM(event, "Correct use is `role-react delete <id>`");
        }

        Optional<RoleReactMessage> messageMaybe = this.handler.getById(maybeId.getAsInt());
        if (messageMaybe.isEmpty()) {
            return respondWithDM(event, "No Role React found with ID " + maybeId.getAsInt());
        }

        this.oven = new RoleReactMessage(messageMaybe.get());
        return respondWithDM(event, "Editing Role Reach message with ID " + maybeId.getAsInt());
    }

    private Mono<?> delete(MessageCreateEvent event, Parameters parameters) {
        OptionalInt maybeId = parameters.getIntParameter(0);

        if (maybeId.isEmpty()) {
            return respondWithDM(event, "Correct use is `role-react delete <id>`");
        }

        return this.handler.deleteById(maybeId.getAsInt())
                .switchIfEmpty(respondWithDM(event, "React was deleted.").then())
                .onErrorResume(ex -> respondWithDM(event, "I ran into an issue deleting your Role React. Sorry. Exception: " + ex.getMessage()).then());
    }
}
