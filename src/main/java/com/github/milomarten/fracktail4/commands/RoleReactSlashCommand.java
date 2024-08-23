package com.github.milomarten.fracktail4.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.milomarten.fracktail4.permissions.discord.DiscordPermissionProvider;
import com.github.milomarten.fracktail4.permissions.discord.DiscordRole;
import com.github.milomarten.fracktail4.platform.discord.react.ReactMessage;
import com.github.milomarten.fracktail4.platform.discord.react.ReactOption;
import com.github.milomarten.fracktail4.platform.discord.react.RoleHandler;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandRegistry;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnBean(SlashCommandRegistry.class) // Slash Command
public class RoleReactSlashCommand implements SlashCommandWrapper {
    private final RoleHandler handler;

    private final ObjectMapper objectMapper;

    private final DiscordPermissionProvider permissions;

    private ReactMessage<Snowflake> oven = null;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("role-react")
                .description("Handle role react commands.")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("view")
                        .description("View all existing role reacts.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build()
                )
                .addOption(ApplicationCommandOptionData.builder()
                        .name("create")
                        .description("Create a role react message.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("discard")
                        .description("Discard all changes to the current Role React.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("delete")
                        .description("Delete the Role React message completely.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("role-react-idx")
                                .description("ID of the role-react to edit")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .required(true)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("edit")
                        .description("Edit a Role React message. Requires publish to make changes take effect.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("role-react-idx")
                                .description("ID of the role-react to edit")
                                .type(ApplicationCommandOption.Type.INTEGER.getValue())
                                .required(true)
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("display")
                        .description("Dump the raw contents of the Role React message so far.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("set")
                        .description("Set an attribute of the role react")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("channel")
                                .description("Channel the Role React is a part of")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("channel-id")
                                        .description("Channel this Role React should be a part of")
                                        .type(ApplicationCommandOption.Type.CHANNEL.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("description")
                                .description("Channel the Role React is a part of")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("description-text")
                                        .description("Flavor text of this Role React")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("publish")
                        .description("Publishes the Role React message in the specified location.")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                        .build())
                .addOption(ApplicationCommandOptionData.builder()
                        .name("choice")
                        .description("Add or remove a choice to the Role React")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("add")
                                .description("Add a choice to the Role React")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("emoji")
                                        .description("Emoji for the react")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .required(true)
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("role")
                                        .description("The role to grant for this emoji")
                                        .type(ApplicationCommandOption.Type.ROLE.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("remove")
                                .description("Remove a choice from the Role React")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("emoji")
                                        .description("Emoji to remove")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .build())
                .build();

    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        if (!permissions.getPermissionsForUser(event.getInteraction().getUser()).contains(DiscordRole.MOD)) {
            return SlashCommands.replyEphemeral(event, "Nice try! Only mods can use that.");
        }

        var firstOption = event.getOptions().get(0).getName();
        return switch (firstOption) {
            case "view" -> view(event);
            case "create" -> create(event);
            case "discard" -> discard(event);
            case "delete" -> delete(event);
            case "edit" -> edit(event);
            case "display" -> display(event);
            case "set" -> set(event);
            case "publish" -> publish(event);
            case "choice" -> choice(event);
            default -> SlashCommands.replyEphemeral(event, "Unknown choice " + firstOption);
        };
    }

    private Mono<Void> view(ChatInputInteractionEvent event) {
        var list = this.handler.getRoleReactMessages().listIterator();
        List<String> selections = new ArrayList<>();
        while (list.hasNext()) {
            var id = list.nextIndex();
            var rm = list.next();
            if (rm != null) {
                var sampler = rm.getOptions().stream()
                                .limit(3)
                                .map(ReactOption::getDescription)
                                .toList();
                selections.add(String.format("- %s : %s...", id, String.join(", ", sampler)));
            }
        }
        if (selections.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "No Role Reacts created yet!");
        } else {
            return SlashCommands.replyEphemeral(event, String.join("\n", selections));
        }
    }

    private Mono<Void> create(ChatInputInteractionEvent event) {
        if (oven != null) {
            SlashCommands.replyEphemeral(event, "There is a role react in progress. Use `/role-react discard` to discard.");
        }

        this.oven = new ReactMessage<>();
        oven.setGuildId(event.getInteraction().getGuildId().orElse(null));
        oven.setChannelId(event.getInteraction().getChannelId());

        return SlashCommands.replyEphemeral(event, "Role React initialized");
    }

    private Mono<Void> discard(ChatInputInteractionEvent event) {
        this.oven = null;
        return SlashCommands.replyEphemeral(event, "Role React discarded!");
    }

    private Mono<Void> delete(ChatInputInteractionEvent event) {
        var id = event.getOption("delete").orElseThrow()
                .getOption("role-react-idx").orElseThrow()
                .getValue().orElseThrow()
                .asLong();
        return this.handler.deleteById((int)id)
                .switchIfEmpty(SlashCommands.replyEphemeral(event, "Role React Deleted."))
                .onErrorResume(ex -> {
                    log.error("Error deleting Role React", ex);
                    return SlashCommands.replyEphemeral(event, "Error deleting the Role React. Check the logs for details");
                });
    }

    private Mono<Void> display(ChatInputInteractionEvent event) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        try {
            String block = String.format("```json\n%s\n```", objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this.oven));
            return SlashCommands.replyEphemeral(event, block);
        } catch (Exception ex) {
            log.error("Unable to render preview for Role React", ex);
            return SlashCommands.replyEphemeral(event, "Error previewing the Role React. Check the logs for details");
        }
    }

    private Mono<Void> publish(ChatInputInteractionEvent event) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        return this.handler.publish(this.oven)
                .flatMap(idx -> SlashCommands.replyEphemeral(event, "Role React has been published. ID is: " + idx))
                .doOnSuccess(msg -> this.oven = null)
                .then()
                .onErrorResume(ex -> {
                    log.error("Unable to publish Role React", ex);
                    return SlashCommands.replyEphemeral(event, "Error publishing the Role React. Check the logs for details");
                });
    }

    private Mono<?> edit(ChatInputInteractionEvent event) {
        long id = event.getOption("edit").orElseThrow()
                .getOption("role-react-idx").orElseThrow()
                .getValue().orElseThrow().asLong();

        var messageMaybe = this.handler.getById((int) id);
        if (messageMaybe.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "No Role React found with ID " + id);
        }

        this.oven = new ReactMessage<>(messageMaybe.get());
        return SlashCommands.replyEphemeral(event, "Now editing Role React " + id);
    }

    private Mono<?> set(ChatInputInteractionEvent event) {
        var firstOption = event.getOption("set").orElseThrow().getOptions().get(0);
        return switch (firstOption.getName()) {
            case "channel" -> setChannel(event, firstOption);
            case "description" -> setDescription(event, firstOption);
            default -> SlashCommands.replyEphemeral(event, "Unknown set option " + firstOption);
        };
    }

    private Mono<?> setChannel(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        return opt.getOption("channel").orElseThrow()
                .getOption("channel-id").orElseThrow()
                .getValue().orElseThrow()
                .asChannel()
                .flatMap(c -> {
                    if (c instanceof TextChannel tc) {
                        oven.setGuildId(tc.getGuildId());
                        oven.setChannelId(tc.getId());
                        return SlashCommands.replyEphemeral(event, "Set channel to " + tc.getMention());
                    } else {
                        return SlashCommands.replyEphemeral(event, "Channel must be a text channel.");
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Unable to set the role react channel", ex);
                    return SlashCommands.replyEphemeral(event,
                            "Error getting the channel information. Check the logs for details");
                });
    }

    private Mono<?> setDescription(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        var text = opt.getOption("description").orElseThrow()
                .getOption("description-text").orElseThrow()
                .getValue().orElseThrow()
                .asString();

        this.oven.setDescription(text);
        return SlashCommands.replyEphemeral(event, "Set description of Role React");
    }

    private Mono<?> choice(ChatInputInteractionEvent event) {
        var firstOption = event.getOption("choice").orElseThrow().getOptions().get(0);
        return switch (firstOption.getName()) {
            case "add" -> addChoice(event, firstOption);
            case "remove" -> removeChoice(event, firstOption);
            default -> SlashCommands.replyEphemeral(event, "Unknown choice option " + firstOption);
        };
    }

    private final Pattern CUSTOM_EMOJI_PATTERN = Pattern.compile("<(a?):([^:]+):([0-9]+)>");

    private Mono<?> addChoice(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        var emojiRaw = opt
                .getOption("emoji").orElseThrow()
                .getValue().orElseThrow()
                .asString();
        var roleMono = opt
                .getOption("role").orElseThrow()
                .getValue().orElseThrow()
                .asRole();

        var matcher = CUSTOM_EMOJI_PATTERN.matcher(emojiRaw);
        ReactionEmoji properEmoji;
        if (matcher.matches()) {
            boolean animated = "a".equals(matcher.group(1));
            String name = matcher.group(2);
            Snowflake id = Snowflake.of(matcher.group(3));
            properEmoji = ReactionEmoji.custom(id, name, animated);
        } else {
            // TODO: Validate this string really is an emoji.
            properEmoji = ReactionEmoji.unicode(emojiRaw);
        }

        if (oven.getOptions().stream().anyMatch(ro -> ro.getEmoji().equals(properEmoji))) {
            return SlashCommands.replyEphemeral(event, "Emoji " + emojiRaw + " is already in use, please pick another.");
        }

        return roleMono
                .flatMap(role -> {
                    ReactOption<Snowflake> option = new ReactOption<>();
                    option.setEmoji(properEmoji);
                    option.setDescription(role.getName());
                    option.setId(role.getId());
                    oven.getOptions()
                            .add(option);
                    return SlashCommands.replyEphemeral(event, "Added option " + emojiRaw + ": " + role.getMention());
                });
    }

    private Mono<?> removeChoice(ChatInputInteractionEvent event, ApplicationCommandInteractionOption opt) {
        if (oven == null) { return SlashCommands.replyEphemeral(event, "Nothing is in progress right now."); }

        var emojiRaw = opt
                .getOption("emoji").orElseThrow()
                .getValue().orElseThrow()
                .asString();

        var matcher = CUSTOM_EMOJI_PATTERN.matcher(emojiRaw);
        ReactionEmoji properEmoji;
        if (matcher.matches()) {
            boolean animated = "a".equals(matcher.group(1));
            String name = matcher.group(2);
            Snowflake id = Snowflake.of(matcher.group(3));
            properEmoji = ReactionEmoji.custom(id, name, animated);
        } else {
            // TODO: Validate this string really is an emoji.
            properEmoji = ReactionEmoji.unicode(emojiRaw);
        }

        var removed = oven.getOptions().removeIf(ro -> ro.getEmoji().equals(properEmoji));
        if (removed) {
            return SlashCommands.replyEphemeral(event, "Role " + emojiRaw + " removed.");
        } else {
            return SlashCommands.replyEphemeral(event, "Role " + emojiRaw + " was not present in the list. Nothing happened");
        }
    }
}
