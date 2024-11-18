package com.github.milomarten.fracktail4.amtrak.parameters;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.milomarten.fracktail4.amtrak.AmtrakCommand;
import com.github.milomarten.fracktail4.amtrak.models.Station;
import com.github.milomarten.fracktail4.amtrak.models.TrainId;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.object.command.ApplicationCommandOption;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.Collectors;

@Data
@JsonTypeName("station")
@Parameter(description = "Lookup an Amtrak or VIA station. VIA-only stations only have codes and names.", type = ApplicationCommandOption.Type.SUB_COMMAND)
public class AmtrakStationLookup implements AmtrakLookup {
    @Parameter(description = "3- or 4-letter Station Code")
    @Size(min = 3, max = 4)
    private String code;
    @Parameter(description = "Station Name. Must be exact, aside from case")
    private String name;
    @Parameter(description = "Name of the station's city. Must be exact, aside from case")
    private String city;
    @Parameter(description = "2-Letter state code")
    @Size(min = 2, max = 2)
    private String state;
    @Parameter(description = "5-digit zip code")
    @Size(min = 5, max = 5)
    private String zip;

    @Override
    public SlashCommandResponse doLookup(AmtrakCommand root) {
        Mono<Station> station;
        if (StringUtils.isNotBlank(code)) {
            // Code is the most precise, and should be used with preference.
            station = root.getLookup().stationByCode(code);
        } else if (StringUtils.isNotBlank(name)) {
            station = root.getLookup().stationByName(name);
        } else if (StringUtils.isNotBlank(zip)) {
            station = root.getLookup().stationByZip(zip);
        } else if (StringUtils.isNoneBlank(city, state)) {
            station = root.getLookup().stationByCityState(city, state);
        } else if (StringUtils.isNotBlank(city)) {
            station = root.getLookup().stationByCity(city);
        } else if (StringUtils.isNotBlank(state)) {
            return Responses.replyEphemeral("State code isn't enough. Please send a city name, too.");
        } else {
            return Responses.replyEphemeral("Some criteria must be passed in order to search.");
        }

        return Responses.delayedReply(
                station.map(s -> {
                    String lineOne = String.format("%s - %s", s.getName(), s.getCode());
                    String lineTwo = s.getAddressLine();

                    var upcomingTrainsPretty = s.getTrains().stream()
                            .collect(Collectors.collectingAndThen(
                                    Collectors.groupingBy(TrainId::trainNumber),
                                    map -> {
                                        return map.entrySet().stream()
                                                .map(entry -> entry.getValue().size() == 1 ?
                                                        entry.getKey() :
                                                        entry.getValue().stream()
                                                                .map(TrainId::dom)
                                                                .map(this::getDepartureDateFromDOM)
                                                                .collect(Collectors.joining(", ", entry.getKey() + " (", ")")))
                                                .collect(Collectors.joining(", "));
                                    }
                                ));

                    String lineThree = String.format("This station has %d upcoming train(s):\n%s",
                            s.getTrains().size(),
                            upcomingTrainsPretty);
                    return lineOne + "\n" + lineTwo + "\n" + lineThree;
                })
                .defaultIfEmpty("Unable to find that station, sorry.")
                .onErrorResume(e -> Mono.just("Unable to find that station, sorry."))
        );
    }

    private String getDepartureDateFromDOM(int dom) {
        var now = LocalDateTime.now();
        Month monthOfDeparture;

        // In context of today, the DOM can be usually be resolved with context, since the system only shows
        // trains in current operation. The major challenge is determining when the month rolls over or backwards.
        // We roll the month back 1 if today's DOM is significantly less than the train's DOM. (Today 1, Train 31: Train is last month)
        // Similarly, we roll the month forward 1 if today's DOM is significantly more than the train's DOM. (Today 31, Train 1: Train is next month)
        // We don't roll the month at all if today's DOM is not significantly more or less than the train's DOM
        // We define "significantly" as more than 4 days.

        if (Math.abs(now.getDayOfMonth() - dom) <= 4) { monthOfDeparture = now.getMonth(); }
        else if (now.getDayOfMonth() < dom) { monthOfDeparture = now.getMonth().minus(1); }
        else { monthOfDeparture = now.getMonth().plus(1); }

        return "Dep " + monthOfDeparture.getValue() + "/" + dom;
    }
}
