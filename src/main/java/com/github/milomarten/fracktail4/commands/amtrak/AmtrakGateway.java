package com.github.milomarten.fracktail4.commands.amtrak;

import com.github.milomarten.fracktail4.commands.amtrak.models.StaleStatus;
import com.github.milomarten.fracktail4.commands.amtrak.models.Station;
import com.github.milomarten.fracktail4.commands.amtrak.models.Train;
import com.github.milomarten.fracktail4.commands.amtrak.models.TrainId;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AmtrakGateway {
    private static final ParameterizedTypeReference<Map<String, Station>> STATION_MAP_BY_CODE = new ParameterizedTypeReference<>(){};
    private static final ParameterizedTypeReference<Map<String, List<Train>>> TRAIN_LIST = new ParameterizedTypeReference<>(){};

    private final WebClient amtrakWebClient;

    public Flux<Station> getAllStations() {
        return amtrakWebClient.get()
                .uri("/stations")
                .exchangeToMono(cr -> cr.bodyToMono(STATION_MAP_BY_CODE))
                .flatMapIterable(m -> m.values());
    }

    public Mono<Station> getStation(String code) {
        return amtrakWebClient.get()
                .uri(u -> u.path("/stations/{id}").build(code))
                .exchangeToMono(cr -> cr.bodyToMono(STATION_MAP_BY_CODE))
                .mapNotNull(m -> m.isEmpty() ? null : m.values().iterator().next());
    }

    public Flux<Train> getAllTrains() {
        return amtrakWebClient.get()
                .uri("/trains")
                .exchangeToMono(cr -> cr.bodyToMono(TRAIN_LIST))
                .flatMapIterable(m -> m.values())
                .flatMapIterable(m -> m);
    }

    public Mono<Train> getTrain(TrainId id) {
        return amtrakWebClient.get()
                .uri(u -> u.path("/trains/{id}").build(id.toString()))
                .exchangeToMono(cr -> cr.bodyToMono(TRAIN_LIST))
                .mapNotNull(l -> l.isEmpty() ? null : l.values().iterator().next())
                .mapNotNull(m -> m.isEmpty() ? null : m.get(0));
    }

    public Flux<Train> getTrains(String id) {
        return amtrakWebClient.get()
                .uri(u -> u.path("/trains/{id}").build(id))
                .exchangeToMono(cr -> cr.bodyToMono(TRAIN_LIST))
                .flatMapIterable(m -> m.values())
                .flatMapIterable(m -> m);
    }

    public Mono<StaleStatus> getStaleStatus() {
        return amtrakWebClient.get()
                .uri("/stale")
                .exchangeToMono(cr -> cr.bodyToMono(StaleStatus.class));
    }
}
