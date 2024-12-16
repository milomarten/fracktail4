package com.github.milomarten.fracktail4.commands.matchup;

import lombok.Getter;

public enum PokemonType {
    NORMAL, FIGHTING, FLYING, POISON, GROUND, ROCK, BUG, GHOST, STEEL,
    FIRE, WATER, GRASS, ELECTRIC, PSYCHIC, ICE, DRAGON, DARK, FAIRY;

    @Getter(lazy = true)
    private static final MatchupTable<PokemonType> MATCHUPS = new MatchupTable<>(PokemonType.class);
}
