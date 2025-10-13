package com.github.milomarten.fracktail5.commands.amtrak;

import com.github.milomarten.fracktail.core.amtrak.AmtrakLookup;
import com.github.milomarten.fracktail.core.amtrak.models.Station;
import com.github.milomarten.fracktail.core.amtrak.models.TrainId;
import com.github.milomarten.fracktail.core.config.TemplateCache;
import com.github.milomarten.fracktail4.commands.amtrak.parameters.AmtrakStationLookup;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmtrakLookupStationSubCommand {
    private final AmtrakLookup lookup;
    private final TemplateCache templateCache;

    @Getter
    private final DiscordArgumentSubCommandDetails<Parameters> details =
            new DiscordArgumentSubCommandDetails<>(
                    "lookup-station",
                    "Lookup an Amtrak, VIA, or Brightline station.",
                    new PojoParser<>(Parameters::new)
                            .addField(
                                    new StringArgumentParser(
                                            "code",
                                            "3- or 4-letter Station Code")
                                            .minLength(3).maxLength(4).defaultToEmpty(),
                                    Parameters::setCode)
                            .addField(
                                    new StringArgumentParser(
                                            "name",
                                            "Station Name. Must be exact, aside from case")
                                            .defaultToEmpty(),
                                    Parameters::setName)
                            .addField(
                                    new StringArgumentParser(
                                            "city",
                                            "Name of the station's city. Must be exact, aside from case")
                                            .defaultToEmpty(),
                                    Parameters::setCity)
                            .addField(
                                    new StringArgumentParser(
                                            "state",
                                            "2-Letter state code")
                                            .minLength(2).maxLength(2).defaultToEmpty(),
                                    Parameters::setState)
                            .addField(
                                    new StringArgumentParser(
                                            "zip",
                                            "5-digit zip code")
                                            .minLength(5).maxLength(5).defaultToEmpty(),
                                    Parameters::setZip)
                    ,
                    this::lookupStation
            );

    private DiscordResponse lookupStation(Parameters parameters) {
        Mono<Station> station;
        if (StringUtils.isNotBlank(parameters.getCode())) {
            // Code is the most precise, and should be used with preference.
            station = lookup.stationByCode(parameters.getCode());
        } else if (StringUtils.isNotBlank(parameters.getName())) {
            station = lookup.stationByName(parameters.getName());
        } else if (StringUtils.isNotBlank(parameters.getZip())) {
            station = lookup.stationByZip(parameters.getZip());
        } else if (StringUtils.isNoneBlank(parameters.getCity(), parameters.getState())) {
            station = lookup.stationByCityState(parameters.getCity(), parameters.getState());
        } else if (StringUtils.isNotBlank(parameters.getCity())) {
            station = lookup.stationByCity(parameters.getCity());
        } else if (StringUtils.isNotBlank(parameters.getState())) {
            return DiscordResponses.replyEphemeral("State code isn't enough. Please send a city name, too.");
        } else {
            return DiscordResponses.replyEphemeral("Some criteria must be passed in order to search.");
        }

        var response = station
                .map(this::forStation)
                .map(summary -> templateCache.apply("station-lookup", summary))
                .defaultIfEmpty("Sorry, I don't know that station.")
                .onErrorReturn("Sorry, I had trouble getting that station.");

        return DiscordResponses.defer(response.map(DiscordResponses::reply));
    }

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static class Parameters {
        String code;
        String name;
        String city;
        String state;
        String zip;
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
