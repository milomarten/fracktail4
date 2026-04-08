package com.github.milomarten.fracktail.core.dice.table;

import lombok.RequiredArgsConstructor;
import org.apache.commons.rng.UniformRandomProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class Rolltables {
    private static final Map<String, RandomlySelected<String>> tables = new HashMap<>();

    static {
        tables.put("cards", Tables.frenchCards().map(Tables.FrenchCard::toString));
        tables.put("loteria", Tables.loteria().map(Tables.Loteria::getValue));
        tables.put("pokemon-types", Tables.pokemonTypes().map(Tables.PokemonType::toString));

        tables.put("pokemon", Tables.pokemon(Tables.PokemonFilterType.ALL)
                .map(PokemonDataSource.Pokemon::getName)
        );
        tables.put("evolved-pokemon", Tables.pokemon(Tables.PokemonFilterType.EVOLVED_ONLY)
                .map(PokemonDataSource.Pokemon::getName)
        );
        tables.put("unevolved-pokemon", Tables.pokemon(Tables.PokemonFilterType.UNEVOLVED_ONLY)
                .map(PokemonDataSource.Pokemon::getName)
        );
    }

    public static String rollTable(String name, UniformRandomProvider urp) {
        var table = tables.get(name);
        if (table == null) {
            return null;
        } else {
            return table.get(urp);
        }
    }
}
