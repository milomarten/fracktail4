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
import java.util.EnumSet;
import java.util.Set;

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

        root.getGateway().getStaleStatus()
                .subscribe(System.out::println);

        return Responses.delayedReply(train
                .map(this::toView)
                .map(view -> root.templateResponse("train-lookup", view))
                .defaultIfEmpty("Sorry, I don't know that train.")
                .onErrorMap(ex -> {
                    log.error("", ex);
                    return ex;
                })
                .onErrorReturn("Sorry, I had trouble getting that train.")
        );

//        return Responses.delayedReply(train
//                        .map(t -> {
//                    // Train <code> is the <heading> <train name>
//                    // It left <station> at <time>, and arrived at its final destination <station> at <time>
//                    // OR It left <station> at <time>, and will arrive at its final destination <station> at <time>
//                    // It is currently in <station>, as of <update time>
//                    var lineOne = String.format("%s Train %s is the %s, heading %s.", t.getProvider(), t.getTrainNum(), t.getRouteName(), t.getHeading());
//                    var firstStation = CollectionUtils.firstElement(t.getStations());
//                    var finalStation = CollectionUtils.lastElement(t.getStations());
//                    if (firstStation == null || finalStation == null) {
//                        return lineOne + "\n" + "The route is empty, I can't provide any more information.";
//                    }
//
//                    String lineTwo;
//                    if (finalStation.getStatus() == RouteStatus.ENROUTE || finalStation.getStatus() == RouteStatus.UNKNOWN) {
//                        lineTwo = String.format("It left %s on %s, and is scheduled to arrive at its final destination of %s on %s.",
//                                firstStation.getName(), FORMATTER.format(firstStation.getDeparture()),
//                                finalStation.getName(), FORMATTER.format(finalStation.getScheduledArrival())
//                        );
//
//                        var upcomingStation = t.getUpcomingStation();
//                        String lineThree = String.format("It is currently approaching %s, as of %s. %s(all times are local time)",
//                                t.getEventName(), FORMATTER.format(t.getUpdatedAt()
//                                        .withZoneSameInstant(t.getEventTimezone().toZoneId())),
//                                upcomingStation == null ? "" :
//                                        "It's estimated to arrive there on " + FORMATTER.format(upcomingStation.getArrival()) + ". "
//                        );
//
//                        return lineOne + "\n" + lineTwo + "\n" + lineThree;
//                    } else {
//                        lineTwo = String.format("It left %s on %s, and arrived at its final destination of %s on %s (all times are local time).",
//                                firstStation.getName(), FORMATTER.format(firstStation.getDeparture()),
//                                finalStation.getName(), FORMATTER.format(finalStation.getArrival())
//                        );
//
//                        return lineOne + "\n" + lineTwo;
//                    }
//                })
//                .switchIfEmpty(Mono.just("Sorry, I don't know that train."))
//                .onErrorResume(e -> Mono.just("Sorry, I don't know that train."))
//        );
    }

    private static final Set<TrainState> IN_MOTION_STATUSES =
            EnumSet.of(TrainState.ACTIVE, TrainState.PREDEPARTURE);

    private TrainView toView(Train train) {
        var firstStation = CollectionUtils.firstElement(train.getStations());
        var lastStation = CollectionUtils.lastElement(train.getStations());
        var upcomingStation = train.getUpcomingStation();

        return new TrainView(
                train.getProvider(),
                train.getTrainNum(),
                train.getRouteName(),
                train.getHeading(),
                StationView.of(firstStation),
                StationView.of(lastStation),
                StationView.of(upcomingStation),
                IN_MOTION_STATUSES.contains(train.getTrainState()),
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
            boolean inMotion,
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
