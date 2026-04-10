package com.github.milomarten.fracktail.core.dice.table;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class PokemonDataSource {
    private static final List<Pokemon> DATA = new ArrayList<>();
    private static final Map<String, String> QUERY = Map.of("query", """
            query normalPokemonList {
                  normals: pokemonspecies(
                    where: {is_mythical:  {
                       _eq: false
                    }, is_legendary:  {
                       _eq: false
                    }}
                    order_by: {id: asc}
                  ) {
                    name
                    id,
                    evolves_from_species_id,
                    pokemonspeciesnames(where:  {
                       language_id:  {
                          _eq: 9
                       }
                    }) {
                      name
                    }
                  }
                }
            """);

    private final WebClient graphQL;

    @PostConstruct
    private void populateData() {
        getData()
                .block();
    }

    @Scheduled(cron = "0 0 8 * * SUN")
    private void populateDataScheduled() {
        getData()
                .onErrorComplete()
                .block();
    }

    private Mono<?> getData() {
        log.info("Repopulating Pokemon list...");
        return graphQL.post()
                .bodyValue(QUERY)
                .exchangeToMono(cr -> cr.bodyToMono(PokemonResponse.class))
                .doOnSuccess(i -> {
                    synchronized (DATA) {
                        DATA.clear();
                        DATA.addAll(i.data.normals);
                    }
                });
    }

    public static <T> T extract(Function<List<Pokemon>, T> function) {
        synchronized (DATA) {
            return function.apply(DATA);
        }
    }

    @Data
    private static class PokemonResponse {
        private PokemonResponseData data;
    }

    @Data
    private static class PokemonResponseData {
        private List<Pokemon> normals;
    }

    @Data
    public static class Pokemon {
        private int id;
        private String name;
        private String englishName;
        private boolean hasEvolved;

        @JsonProperty("evolves_from_species_id")
        private void setHasEvolved(Integer i) {
            this.hasEvolved = (i != null);
        }

        @JsonProperty("pokemonspeciesnames")
        private void setEnglishName(List<Name> names) {
            this.englishName = names.isEmpty() ? null : names.get(0).name;
        }

        public String getEnglishName() {
            return Objects.requireNonNullElse(this.englishName, this.name);
        }
    }

    @Data
    private static class Name {
        private String name;
    }

    @Data
    private static class EvolutionChain {
        private List<Name> pokemonspecies;
    }
}
