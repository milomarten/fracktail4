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

import java.util.List;

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

        return Responses.delayedReply(station.map(this::forStation)
                    .map(summary -> root.templateResponse("station-lookup", summary)));
    }

    private Summary forStation(Station station) {
        var trainNumbersAndDOMs = station.getTrains()
                .stream()
                .map(TrainId::toString)
                .toList();
        var address = new Address(station.getAddress1(), station.getAddress2(), station.getCity(), station.getState(), station.getZip());
        return new Summary(station.getCode(), station.getName(), address, trainNumbersAndDOMs);
    }

    public record Summary(String code, String name, Address address, List<String> trains) {
        public int numberOfTrains() { return trains.size(); }
    }
    public record Address(String line1, String line2, String city, String state, String zip) {
        public boolean exists() {
            return StringUtils.isNoneBlank(line1, city, state, zip);
        }

        public boolean hasLine2() {
            return StringUtils.isNotBlank(line2);
        }
    }
}
