package com.github.milomarten.fracktail5.commands.amtrak;

import com.github.milomarten.fracktail.core.amtrak.AmtrakGateway;
import com.github.milomarten.fracktail.core.amtrak.models.*;
import com.github.milomarten.fracktail.core.config.TemplateCache;
import com.github.milomarten.fracktail5.platform.discord.DiscordResponse;
import com.github.milomarten.fracktail5.platform.discord.argument.IntArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.argument.PojoParser;
import com.github.milomarten.fracktail5.platform.discord.argument.StringArgumentParser;
import com.github.milomarten.fracktail5.platform.discord.subcommand.DiscordArgumentSubCommandDetails;
import com.github.milomarten.fracktail5.platform.discord.util.DiscordResponses;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AmtrakLookupTrainSubCommand {
    private final AmtrakGateway gateway;
    private final TemplateCache templateCache;

    @Getter private final DiscordArgumentSubCommandDetails<Parameters> details =
            new DiscordArgumentSubCommandDetails<>(
            "lookup-train",
            "Lookup an Amtrak, VIA, or Brightline train",
            new PojoParser<>(Parameters::new)
                    .addField(
                            new StringArgumentParser("trainnumber", "The code identifying this train")
                                    .required(),
                            Parameters::setTrainNumber)
                    .addField(
                            new IntArgumentParser("dayofmonth", "The day of the month the train departed")
                                    .min(1).max(31)
                                    .defaultTo(null),
                            Parameters::setDayOfMonth),
            this::lookupTrain
    );

    private DiscordResponse lookupTrain(Parameters parameters) {
        Mono<Train> train;
        if (parameters.getDayOfMonth() != null) {
            train = gateway.getTrain(new TrainId(parameters.getTrainNumber(), parameters.getDayOfMonth()));
        } else {
            train = gateway.getTrains(parameters.getTrainNumber()).next();
        }

        var responseMono = train
                .map(this::toView)
                .map(view -> {
                    if (view.state() == TrainState.PREDEPARTURE) {
                        return templateCache.apply("amtrak/train-lookup-predeparture", view);
                    } else if (view.state() == TrainState.COMPLETED) {
                        return templateCache.apply("amtrak/train-lookup-completed", view);
                    } else if (view.state() == TrainState.AT_STATION) {
                        return templateCache.apply("amtrak/train-lookup-at-station", view);
                    } else {
                        return templateCache.apply("amtrak/train-lookup", view);
                    }
                })
                .map(m -> m + "\n-# All times are in local time.")
                .defaultIfEmpty("Sorry, I don't know that train.")
                .onErrorMap(ex -> {
                    log.error("", ex);
                    return ex;
                })
                .onErrorReturn("Sorry, I had trouble getting that train.");

        return DiscordResponses.delayedResponse(responseMono);
    }

    @Data
    private static class Parameters {
        private String trainNumber;
        private Integer dayOfMonth;
    }

    private TrainView toView(Train train) {
        var firstStation = CollectionUtils.firstElement(train.getStations());
        var lastStation = CollectionUtils.lastElement(train.getStations());
        var upcomingStation = train.getEventStation();
        if (upcomingStation.getStatus() == RouteStatus.STATION) {
            train.setTrainState(TrainState.AT_STATION);
        }

        return new TrainView(
                train.getProvider(),
                train.getTrainNum(),
                train.getRouteName(),
                train.getHeading(),
                StationView.of(firstStation),
                StationView.of(lastStation),
                StationView.of(upcomingStation),
                train.getTrainState(),
                train.getVelocity(),
                train.getUpdatedAt().withZoneSameInstant(train.getEventTimezone().toZoneId())
        );
    }

    public record TrainView(
            TrainProvider provider,
            String id,
            String name,
            String bearing,
            StationView origin,
            StationView finalDestination,
            StationView nextDestination,
            TrainState state,
            double speed,
            ZonedDateTime lastEvent
    ) {
        public boolean hasRouteInfo() {
            return origin != null && finalDestination != null;
        }

        public boolean hasNextDestination() {
            return nextDestination != null;
        }
    }

    public record StationView(
            String code,
            String name,
            DepartureArrival times
    ) {
        public static StationView of(RouteStation station) {
            if (station == null) return null;
            return new StationView(station.getCode(), station.getName(), DepartureArrival.of(station));
        }
    }

    public record DepartureArrival(ZonedDateTime schDeparture, ZonedDateTime schArrival,
                                   ZonedDateTime estDeparture, ZonedDateTime estArrival) {
        public static DepartureArrival of(RouteStation rs) {
            if (rs == null) { return null; }
            if (rs.getCode().length() == 4) {
                // Non-Amtrak station times are in UTC, so we have to normalize with the timezone sadly.
                // VIA and Brightline stations are all four-letters
                var timezone = rs.getTimezone().toZoneId();
                return new DepartureArrival(
                        rs.getScheduledDeparture().withZoneSameInstant(timezone),
                        rs.getScheduledArrival().withZoneSameInstant(timezone),
                        rs.getDeparture().withZoneSameInstant(timezone),
                        rs.getArrival().withZoneSameInstant(timezone)
                );
            }
            return new DepartureArrival(
                    rs.getScheduledDeparture(), rs.getScheduledArrival(),
                    rs.getDeparture(), rs.getArrival()
            );
        }

        public Duration delay() {
            if (schArrival == null || estArrival == null) return Duration.ZERO;
            return Duration.between(schArrival, estArrival);
        }
    }
}
