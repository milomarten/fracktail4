package com.github.milomarten.fracktail.core.dice.table;

import com.github.milomarten.fracktail.core.dice.table.multi.MultiRandomlySelected;
import com.github.milomarten.fracktail.core.dice.table.multi.MultiRandomlySelectedWrapper;
import com.github.milomarten.fracktail.core.dice.table.multi.MultiRollParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class Rolltables {
    private static final Map<String, MultiRandomlySelected<String>> tables = new HashMap<>();

    static {
        put("cards", Tables.frenchCards().map(Tables.FrenchCard::toString));
        put("loteria", Tables.loteria().map(Tables.Loteria::getValue));
        put("pokemon-types", Tables.pokemonTypes().map(Tables.PokemonType::toString));

        put("pokemon", Tables.pokemon(Tables.PokemonFilterType.ALL)
                .map(PokemonDataSource.Pokemon::getEnglishName)
        );
        put("evolved-pokemon", Tables.pokemon(Tables.PokemonFilterType.EVOLVED_ONLY)
                .map(PokemonDataSource.Pokemon::getEnglishName)
        );
        put("unevolved-pokemon", Tables.pokemon(Tables.PokemonFilterType.UNEVOLVED_ONLY)
                .map(PokemonDataSource.Pokemon::getEnglishName)
        );
    }

    private static void put(String name, RandomlySelected<String> table) {
        if (table instanceof MultiRandomlySelected<String> mrs) {
            tables.put(name, mrs);
        } else {
            tables.put(name, new MultiRandomlySelectedWrapper<>(table));
        }
    }

    public static String rollMultitable(String name, MultiRollParams params) {
        var table = tables.get(name);
        if (table == null) {
            return null;
        } else {
            return String.join(", ", table.getMultiple(params));
        }
    }
}
