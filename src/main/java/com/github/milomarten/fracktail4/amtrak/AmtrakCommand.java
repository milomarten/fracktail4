package com.github.milomarten.fracktail4.amtrak;

import com.github.milomarten.fracktail4.amtrak.models.RouteStatus;
import com.github.milomarten.fracktail4.amtrak.models.Station;
import com.github.milomarten.fracktail4.amtrak.models.Train;
import com.github.milomarten.fracktail4.amtrak.models.TrainId;
import com.github.milomarten.fracktail4.platform.discord.slash.SlashCommandWrapper;
import com.github.milomarten.fracktail4.platform.discord.utils.SlashCommands;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.DurationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// /amtrak lookup station <city> <state> <zip> <code> <name> -> Code and info
// /amtrak lookup train <name> -> Codes and departure times
// /amtrak next train-in <code> -> Next train coming in
@Component
@RequiredArgsConstructor
public class AmtrakCommand implements SlashCommandWrapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd 'at' hh:mm a");

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
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("station")
                                .description("Lookup an Amtrak or VIA station. VIA-only stations only have codes and names.")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("code")
                                        .description("3- or 4-letter Station Code")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .minLength(3).maxLength(4)
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("name")
                                        .description("Station Name. Must be exact, aside from case")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("city")
                                        .description("Name of the station's city. Must be exact, aside from case")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("state")
                                        .description("2-Letter state code")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .minLength(2).maxLength(2)
                                        .build())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("zip")
                                        .description("5-digit zip code")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .minLength(5).maxLength(5)
                                        .build())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("route")
                                .description("Lookup an Amtrak or VIA route")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("route-name")
                                        .description("The name of the rail line")
                                        .type(ApplicationCommandOption.Type.STRING.getValue())
                                        .required(true)
                                        .build())
                                .build())
                        .addOption(ApplicationCommandOptionData.builder()
                                .name("train")
                                .description("Lookup an Amtrak or VIA train")
                                .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
                                .addOption(ApplicationCommandOptionData.builder()
                                        .name("train-number")
                                        .description("The code identifying this train")
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
            case "route" -> handleRouteLookup(event, param.getOption(second).get());
            case "station" -> handleStationLookup(event, param.getOption(second).get());
            case "train" -> handleTrainLookup(event, param.getOption(second).get());
            default -> SlashCommands.replyEphemeral(event, "Need to provide subcommand station/train");
        };
    }

    private Mono<?> handleRouteLookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption param) {
        var name = param.getOption("route-name")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString());
        if (name.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "Route name is required");
        }

        return event.deferReply()
                .thenMany(lookup.trainsByRouteName(name.get()))
                .collectList()
                .map(listOfTrains -> {
                    if (listOfTrains.isEmpty()) {
                        return "I can't find a route with that name.";
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

    private Mono<?> handleStationLookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption parameters) {
        var code = parameters.getOption("code").flatMap(a -> a.getValue()).map(a -> a.asString());
        var name = parameters.getOption("name").flatMap(a -> a.getValue()).map(a -> a.asString());
        var city = parameters.getOption("city").flatMap(a -> a.getValue()).map(a -> a.asString());
        var state = parameters.getOption("state").flatMap(a -> a.getValue()).map(a -> a.asString());
        var zip = parameters.getOption("zip").flatMap(a -> a.getValue()).map(a -> a.asString());

        Mono<Station> station;
        if (code.isPresent()) {
            // Code is the most precise, and should be used with preference.
            station = lookup.stationByCode(code.get());
        } else if (name.isPresent()) {
            station = lookup.stationByName(name.get());
        } else if (zip.isPresent()) {
            station = lookup.stationByZip(zip.get());
        } else if (city.isPresent() && state.isPresent()) {
            station = lookup.stationByCityState(city.get(), state.get());
        } else if (city.isPresent()) {
            station = lookup.stationByCity(city.get());
        } else if (state.isPresent()) {
            return SlashCommands.replyEphemeral(event, "State code isn't enough. Please send a city name, too.");
        } else {
            return SlashCommands.replyEphemeral(event, "Some criteria must be passed in order to search.");
        }

        return event.deferReply()
                .then(station)
                .map(s -> {
                    // Toronto Union - TWO
                    // Address, City, State, Zip
                    // N upcoming trains
                    String lineOne = String.format("%s - %s", s.getName(), s.getCode());
                    String lineTwo = s.getAddressLine();
                    String lineThree = String.format("This station has %d upcoming train(s): %s",
                            s.getTrains().size(),
                            s.getTrains().stream().map(TrainId::trainNumber)
                                    .distinct().collect(Collectors.joining(", ")));
                    return lineOne + "\n" + lineTwo + "\n" + lineThree;
                })
                .defaultIfEmpty("Unable to find that station, sorry.")
                .onErrorResume(e -> Mono.just("Unable to find that station, sorry."))
                .flatMap(event::createFollowup);
    }

    private Mono<?> handleTrainLookup(ChatInputInteractionEvent event, ApplicationCommandInteractionOption param) {
        var number = param.getOption("train-number")
                .flatMap(a -> a.getValue())
                .map(a -> a.asString());
        var dom = param.getOption("day-of-month")
                .flatMap(a -> a.getValue())
                .map(a -> a.asLong());
        if (number.isEmpty()) {
            return SlashCommands.replyEphemeral(event, "Route name is required");
        }

        Mono<Train> train;
        if (dom.isPresent()) {
            train = gateway.getTrain(new TrainId(number.get(), dom.get().intValue()));
        } else {
            train = gateway.getTrains(number.get()).next();
        }
        return event.deferReply()
                .then(train)
                .map(t -> {
                    // Train <code> is the <heading> <train name>
                    // It left <station> at <time>, and arrived at its final destination <station> at <time>
                    // OR It left <station> at <time>, and will arrive at its final destination <station> at <time>
                    // It is currently in <station>, as of <update time>
                    var lineOne = String.format("%s Train %s is the %s, heading %s.", t.getProvider(), t.getTrainNum(), t.getRouteName(), t.getHeading());
                    var firstStation = CollectionUtils.firstElement(t.getStations());
                    var finalStation = CollectionUtils.lastElement(t.getStations());
                    if (firstStation == null || finalStation == null) {
                        return lineOne + "\n" + "The route is empty, I can't provide any more information.";
                    }

                    String lineTwo;
                    if (finalStation.getStatus() == RouteStatus.ENROUTE || finalStation.getStatus() == RouteStatus.UNKNOWN) {
                        lineTwo = String.format("It left %s on %s, and is scheduled to arrive at its final destination of %s on %s.",
                                firstStation.getName(), FORMATTER.format(firstStation.getDeparture()),
                                finalStation.getName(), FORMATTER.format(finalStation.getScheduledArrival())
                        );

                        var upcomingStation = t.getUpcomingStation();
                        String lineThree = String.format("It is currently approaching %s, as of %s. %s(all times are local time)",
                                t.getEventName(), FORMATTER.format(t.getUpdatedAt()
                                        .withZoneSameInstant(t.getEventTimezone().toZoneId())),
                                upcomingStation == null ? "" :
                                        "It's scheduled to arrive there on " + FORMATTER.format(upcomingStation.getScheduledArrival()) + ". "
                        );

                        return lineOne + "\n" + lineTwo + "\n" + lineThree;
                    } else {
                        lineTwo = String.format("It left %s on %s, and arrived at its final destination of %s on %s (all times are local time).",
                                firstStation.getName(), FORMATTER.format(firstStation.getDeparture()),
                                finalStation.getName(), FORMATTER.format(finalStation.getArrival())
                        );

                        return lineOne + "\n" + lineTwo;
                    }
                })
                .switchIfEmpty(Mono.just("Sorry, I don't know that train."))
                .onErrorResume(e -> Mono.just("Sorry, I don't know that train."))
                .flatMap(event::createFollowup);
    }
}
