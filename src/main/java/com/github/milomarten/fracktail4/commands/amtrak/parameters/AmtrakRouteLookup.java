package com.github.milomarten.fracktail4.commands.amtrak.parameters;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.milomarten.fracktail4.commands.amtrak.AmtrakCommand;
import com.github.milomarten.fracktail4.commands.amtrak.models.Train;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.object.command.ApplicationCommandOption;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Data
@JsonTypeName("route")
@Parameter(description = "Lookup an Amtrak or VIA route", type = ApplicationCommandOption.Type.SUB_COMMAND)
public class AmtrakRouteLookup implements AmtrakLookup {
    @Parameter(description = "The name of the rail line")
    @NotNull
    private String routename;

    @Override
    public SlashCommandResponse doLookup(AmtrakCommand root) {
        return Responses.delayedReply(
                root.getLookup().trainsByRouteName(routename)
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
                }));
    }
}
