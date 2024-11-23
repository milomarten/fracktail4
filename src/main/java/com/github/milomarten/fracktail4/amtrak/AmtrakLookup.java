package com.github.milomarten.fracktail4.amtrak;

import com.github.milomarten.fracktail4.amtrak.models.Station;
import com.github.milomarten.fracktail4.amtrak.models.Train;
import com.github.milomarten.fracktail4.amtrak.models.TrainId;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@Service
public class AmtrakLookup {
    private AmtrakGateway gateway;
    private Mono<List<Station>> stationsCache;
    private Mono<List<Train>> trainsCache;

    public AmtrakLookup(AmtrakGateway gateway) {
        this.gateway = gateway;
        this.stationsCache = Mono.defer(() -> gateway.getAllStations().collectList())
                .cache(Duration.ofHours(24));
        this.trainsCache = Mono.defer(() -> gateway.getAllTrains().collectList())
                .cache(Duration.ofMinutes(10));
    }

    public Mono<Station> stationByCode(String code) {
        return this.gateway.getStation(code);
    }

    public Mono<Station> stationByName(String name) {
        return stationsCache
                .flatMapIterable(Function.identity())
                .filter(s -> name.equalsIgnoreCase(s.getName()))
                .next();
    }

    public Mono<Station> stationByCity(String city) {
        return stationsCache
                .flatMapIterable(Function.identity())
                .filter(s -> city.equalsIgnoreCase(s.getCity()))
                .next();
    }

    public Mono<Station> stationByCityState(String city, String state) {
        return stationsCache
                .flatMapIterable(Function.identity())
                .filter(s -> city.equalsIgnoreCase(s.getCity()) && state.equalsIgnoreCase(s.getState()))
                .next();
    }

    public Mono<Station> stationByZip(String zipCode) {
        return stationsCache
                .flatMapIterable(Function.identity())
                .filter(s -> zipCode.equals(s.getZip()))
                .next();
    }

    public Flux<Train> trainsByRouteName(String name) {
        return this.trainsCache
                .flatMapIterable(Function.identity())
                .filter(t -> name.equalsIgnoreCase(t.getRouteName()));
    }

    public Flux<Train> trainsByNumbers(Set<TrainId> trainIds) {
        return this.trainsCache
                .flatMapIterable(Function.identity())
                .filter(train -> trainIds.contains(train.getTrainId()));
    }
}
