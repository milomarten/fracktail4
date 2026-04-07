package com.github.milomarten.fracktail.core.dice.table;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Stream;

public class Tables {
    enum FrenchSuit {
        HEARTS, DIAMONDS, SPADES, CLUBS
    }

    enum FrenchValue {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT,
        NINE, TEN, JACK, QUEEN, KING
    }

    public record FrenchCard(FrenchSuit suit, FrenchValue value) {
        @Override
        public String toString() {
            return value + " of " + suit;
        }
    }

    /**
     * Get all the cards in the standard deck of cards
     * @return The cards, in value order and suit order.
     */
    public static RandomlySelected<FrenchCard> frenchCards() {
        var cards = Arrays.stream(FrenchValue.values())
                .flatMap(value -> Arrays.stream(FrenchSuit.values()).map(suit -> new FrenchCard(suit, value)))
                .toList();
        return new UnweightedTable<>(cards);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Loteria {
        EL_GALLO("El gallo"),
        EL_DIABLITO("El diablito"),
        LA_DAMA("La dama"),
        EL_CATRIN("El catrín"),
        EL_PARAGUAS("El paraguas"),
        LA_SIRENA("La sirena"),
        LA_ESCALERA("La escalera"),
        LA_BOTELLA("La botella"),
        EL_BARRIL("El barril"),
        EL_ARBOL("El árbol"),
        EL_MELON("El melón"),
        EL_VALIENTE("El valiente"),
        EL_GORRITO("El gorrito"),
        LA_MUERTE("La muerte"),
        LA_PERA("La pera"),
        LA_BANDERA("La bandera"),
        EL_BANDOLON("El bandolón"),
        EL_VIOLONCELLO("El violoncello"),
        LA_GARZA("La garza"),
        EL_PAJARO("El pájaro"),
        LA_MANO("La mano"),
        LA_BOTA("La bota"),
        LA_LUNA("La luna"),
        EL_COTORRO("El cotorro"),
        EL_BORRACHO("El borracho"),
        EL_NEGRITO("El negrito"),
        EL_CORAZON("El corazón"),
        LA_SANDIA("La sandía"),
        EL_TAMBOR("El tambor"),
        EL_CAMARON("El camarón"),
        LAS_JARAS("Las jaras"),
        EL_MUSICO("El músico"),
        LA_ARANA("La araña"),
        EL_SOLDADO("El soldado"),
        LA_ESTRELLA("La estrella"),
        EL_CAZO("El cazo"),
        EL_MUNDO("El mundo"),
        EL_APACHE("El Apache"),
        EL_NOPAL("El nopal"),
        EL_ALACRAN("El alacrán"),
        LA_ROSA("La rosa"),
        LA_CALAVERA("La calavera"),
        LA_CAMPANA("La campana"),
        EL_CANTARITO("El cantarito"),
        EL_VENADO("El venado"),
        EL_SOL("El Sol"),
        LA_CORONA("La corona"),
        LA_CHALUPA("La chalupa"),
        EL_PINO("El pino"),
        EL_PESCADO("El pescado"),
        LA_PALMA("La palma"),
        LA_MACETA("La maceta"),
        EL_ARPA("El arpa"),
        LA_RANA("La rana");

        private final String value;
    }

    public static RandomlySelected<Loteria> loteria() {
        return new UnweightedTable<>(Arrays.asList(Loteria.values()));
    }

    public enum PokemonType {
        NORMAL, FIGHTING, FLYING, POISON, GROUND, ROCK, BUG, GHOST, STEEL,
        FIRE, WATER, GRASS, ELECTRIC, PSYCHIC, ICE, DARK, DRAGON, FAIRY
    }

    public static RandomlySelected<PokemonType> pokemonTypes() {
        return new UnweightedTable<>(Arrays.asList(PokemonType.values()));
    }
}
