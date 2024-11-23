package com.github.milomarten.fracktail4.amtrak.parameters;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.github.milomarten.fracktail4.amtrak.AmtrakCommand;
import com.github.milomarten.fracktail4.amtrak.models.*;
import com.github.milomarten.fracktail4.platform.discord.mapper.annotations.Parameter;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.Responses;
import com.github.milomarten.fracktail4.platform.discord.slash.adapter.SlashCommandResponse;
import discord4j.core.object.command.ApplicationCommandOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Data
@JsonTypeName("train")
@Slf4j
@Parameter(description = "Lookup an Amtrak or VIA train", type = ApplicationCommandOption.Type.SUB_COMMAND)
public class AmtrakTrainLookup implements AmtrakLookup {
    @Parameter(description = "The code identifying this train")
    @NotNull
    private String trainnumber;

    @Parameter(description = "The day of the month the train departed")
    @Min(1)
    @Max(31)
    private Integer dayofmonth;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd 'at' hh:mm a");

    @Override
    public SlashCommandResponse doLookup(AmtrakCommand root) {
        Mono<Train> train;
        if (dayofmonth != null) {
            train = root.getGateway().getTrain(new TrainId(trainnumber, dayofmonth));
        } else {
            train = root.getGateway().getTrains(trainnumber).next();
        }

        return Responses.delayedReply(train
                .map(this::toView)
                .map(view -> {
                    if (view.state() == TrainState.PREDEPARTURE) {
                        return root.templateResponse("train-lookup-predeparture", view);
                    } else if (view.state() == TrainState.COMPLETED) {
                        return root.templateResponse("train-lookup-completed", view);
                    } else if (view.state() == TrainState.AT_STATION) {
                        return root.templateResponse("train-lookup-station", view);
                    } else {
                        return root.templateResponse("train-lookup", view);
                    }
                })
                        .map(m -> m + "\n-# All times are in local time.")
                .defaultIfEmpty("Sorry, I don't know that train.")
                .onErrorMap(ex -> {
                    log.error("", ex);
                    return ex;
                })
                .onErrorReturn("Sorry, I had trouble getting that train.")
        );
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
