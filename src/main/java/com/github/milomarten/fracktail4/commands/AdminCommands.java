package com.github.milomarten.fracktail4.commands;

import com.github.milomarten.fracktail4.persistence.PersistenceBean;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AdminCommands implements SlashCommandWrapper {
    private final List<PersistenceBean> persistenceBeans;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("admin")
                .description("Admin Stuff")
                .defaultMemberPermissions("0")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("command")
                        .description("The action to perform")
                        .type(ApplicationCommandOption.Type.STRING.getValue())
                        .required(true)
                        .addAllChoices(getChoices())
                        .build()
                )
                .build();
    }

    private List<ApplicationCommandOptionChoiceData> getChoices() {
        return List.of(
                choice("FORCE_PERSIST"),
                choice("FORCE_LOAD")
        );
    }

    private ApplicationCommandOptionChoiceData choice(String value) {
        return ApplicationCommandOptionChoiceData.builder()
                .name(value)
                .value(value)
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var what = event.getOption("command").orElseThrow().getValue().orElseThrow().asString();
        return switch (what) {
            case "FORCE_PERSIST" -> persistAll(event);
            case "FORCE_LOAD" -> loadAll(event);
            default -> SlashCommands.replyEphemeral(event, "???");
        };
    }

    private Mono<?> persistAll(ChatInputInteractionEvent event) {
        var persist = Flux.fromIterable(persistenceBeans)
                .flatMap(PersistenceBean::store)
                .then();

        return event.deferReply().withEphemeral(true)
                .then(persist)
                .then(event.createFollowup("Persistence complete."));
    }

    private Mono<?> loadAll(ChatInputInteractionEvent event) {
        var persist = Flux.fromIterable(persistenceBeans)
                .flatMap(PersistenceBean::load)
                .then();

        return event.deferReply().withEphemeral(true)
                .then(persist)
                .then(event.createFollowup("Load complete."));
    }
}
