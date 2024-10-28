package com.github.milomarten.fracktail4.amtrak;

import com.github.milomarten.fracktail4.amtrak.models.Train;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

// /amtrak lookup station <city> <state> <zip> <code> <name> -> Code and info
// /amtrak lookup train <name> -> Codes and departure times
// /amtrak next train-in <code> -> Next train coming in
@Component
@RequiredArgsConstructor
public class AmtrakCommand implements SlashCommandWrapper {
    private final AmtrakLookup lookup;
    private final AmtrakGateway gateway;

    @Override
    public ApplicationCommandRequest getRequest() {
        return ApplicationCommandRequest.builder()
                .name("amtrak")
                .description("Interact with the Amtrak API. Also supports VIA!")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("lookup")
                        .description("Lookup Amtrak or VIA info")
                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
//                        .addOption(ApplicationCommandOptionData.builder()
//                                .name("station")
//                                .description("Lookup an Amtrak or VIA station")
//                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
//                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("train")
                                .description("Lookup an Amtrak or VIA train")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("train-name")
                                        .description("The name of the rail line")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .build())
//                .addOption(ApplicationCommandOptionData.builder()
//                        .name("next")
//                        .description("Get info on the next arriving train")
//                        .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
//                        .addOption(ApplicationCommandOptionData.builder()
//                                .name("station-id")
//                                .description("The three/four letter station code")
//                                .type(ApplicationCommandOption.Type.STRING.getValue())
//                                .minLength(3).maxLength(4)
//                                .build())
//                        .build())
                .build();
    }

    @Override
    public Mono<?> handleEvent(ChatInputInteractionEvent event) {
        var first = event.getOptions().get(0)
                .getName();
        return switch (first) {
            case "lookup" -> handleLookup(event, event.getOption(first).get());
            case "next" -> Mono.empty();
            default -> SlashCommands.replyEphemeral(event, "Unknown subcommand " + first);
        };
    }

    private Mono<?> handleLookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption param) {
        var second = param.getOptions().get(0).getName();
        return switch (second) {
            case "train" -> handleTrainLookup(event, param.getOption(second).get());
            default -> SlashCommands.replyEphemeral(event, "Need to provide subcommand station/train");
        };
    }

    private Mono<?> handleTrainLookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption param) {
        var name = param.getOption("train-name")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString());
        if (name.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "Train name is required");
        }

        return event.deferReply()
                .thenMany(lookup.trainsByRouteName(name.get()))
                .collectList()
                .map(listOfTrains -> {
                    if (listOfTrains.isEmpty()) {
                        return "I can't find a train with that name.";
                    }
                    var friendlyName = listOfTrains.get(0).getRouteName();

                    var preface = String.format("There are %d upcoming trains called the %s ", listOfTrains.size(), friendlyName);

                    var trainODByNumber = new TreeMap<>(listOfTrains.stream()
                            .collect(Collectors.groupingBy(t -> t.getOriginName() + " to " + t.getDestinationName())));
                    StringJoiner numbersOAndD = new StringJoiner("\n");
                    trainODByNumber.forEach((od, trains) -> {
                        numbersOAndD.add(String.format("- From %s: %s", od, trains.stream().map(Train::getTrainNum).collect(Collectors.joining(", "))));
                    });

                    return preface + ".\nThe train numbers are:\n" + numbersOAndD;
                })
                .flatMap(msg -> SlashCommands.followup(event, msg));
    }
}
