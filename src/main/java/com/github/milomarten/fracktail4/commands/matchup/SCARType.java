package com.github.milomarten.fracktail4.commands.matchup;

import lombok.Getter;
import org.apache.commons.text.WordUtils;

public enum SCARType {
    ASTRAL, BASIC, EARTH, ELECTRIC,
    FIRE, LIGHT, METAL, MYTHIC,
    NATURE, NECROTIC, POISON, PSIONIC,
    SHADOW, SONIC, WATER, WIND;



    @Getter(lazy = true)
    private static final MatchupTable<SCARType> MATCHUPS = new MatchupTable<>(SCARType.class)
            .addBulk(ASTRAL, Matchup.INEFFECTIVE, ASTRAL, PSIONIC, SONIC, WATER, WIND)
            .addBulk(ASTRAL, Matchup.SUPER_EFFECTIVE, EARTH, LIGHT, MYTHIC, NECROTIC)
            .addBulk(EARTH, Matchup.INEFFECTIVE, ASTRAL, EARTH, ELECTRIC, NECROTIC, POISON)
            .addBulk(EARTH, Matchup.SUPER_EFFECTIVE, METAL, NATURE, PSIONIC, WATER)
            .addBulk(ELECTRIC, Matchup.INEFFECTIVE, ELECTRIC, METAL, NATURE, SHADOW)
            .addBulk(ELECTRIC, Matchup.SUPER_EFFECTIVE, SONIC, WATER, WIND)
            .addBulk(FIRE, Matchup.INEFFECTIVE, FIRE, LIGHT, METAL, NATURE, NECROTIC, POISON)
            .addBulk(FIRE, Matchup.SUPER_EFFECTIVE, EARTH, PSIONIC, SHADOW, WATER, WIND)
            .addBulk(LIGHT, Matchup.INEFFECTIVE, ASTRAL, LIGHT, POISON, SONIC, WIND)
            .addBulk(LIGHT, Matchup.SUPER_EFFECTIVE, EARTH, MYTHIC, NECROTIC, SHADOW)
            .addBulk(METAL, Matchup.INEFFECTIVE, LIGHT, METAL, MYTHIC, POISON, PSIONIC)
            .addBulk(METAL, Matchup.SUPER_EFFECTIVE, ELECTRIC, FIRE, NATURE, SONIC)
            .addBulk(MYTHIC, Matchup.INEFFECTIVE, EARTH, ELECTRIC, FIRE, MYTHIC, WATER)
            .addBulk(MYTHIC, Matchup.SUPER_EFFECTIVE, ASTRAL, LIGHT, METAL, POISON)
            .addBulk(NATURE, Matchup.INEFFECTIVE, EARTH, LIGHT, NATURE, SONIC, WATER)
            .addBulk(NATURE, Matchup.SUPER_EFFECTIVE, FIRE, METAL, NECROTIC, POISON)
            .addBulk(NECROTIC, Matchup.INEFFECTIVE, ASTRAL, ELECTRIC, NATURE, NECROTIC, PSIONIC)
            .addBulk(NECROTIC, Matchup.SUPER_EFFECTIVE, FIRE, LIGHT, MYTHIC, SHADOW)
            .addBulk(POISON, Matchup.INEFFECTIVE, ASTRAL, ELECTRIC, MYTHIC, NECROTIC, POISON)
            .addBulk(POISON, Matchup.SUPER_EFFECTIVE, EARTH, SHADOW, WATER, WIND)
            .addBulk(PSIONIC, Matchup.INEFFECTIVE, EARTH, FIRE, NECROTIC, PSIONIC, SHADOW, WIND)
            .addBulk(PSIONIC, Matchup.SUPER_EFFECTIVE, ASTRAL, METAL, MYTHIC, POISON, SONIC)
            .addBulk(SHADOW, Matchup.INEFFECTIVE, METAL, MYTHIC, NATURE, SHADOW, SONIC, WIND)
            .addBulk(SHADOW, Matchup.SUPER_EFFECTIVE, ELECTRIC, FIRE, LIGHT, NECROTIC, PSIONIC);

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());
    }
}
