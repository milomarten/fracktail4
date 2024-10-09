package com.github.milomarten.fracktail4.commands.dnd;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class TypeMatchup {
    private static final Map<Type, Map<Type, Matchup>> matchups;

    static {
        matchups = new EnumMap<>(Type.class);
        matchup(Type.NORMAL, Type.ROCK, Matchup.HALF);
        matchup(Type.NORMAL, Type.GHOST, Matchup.IMMUNE);
        matchup(Type.NORMAL, Type.STEEL, Matchup.HALF);
        bulkMatchup(Type.FIRE, Matchup.HALF, Type.FIRE, Type.WATER, Type.ROCK, Type.DRAGON);
        bulkMatchup(Type.FIRE, Matchup.SUPER, Type.GRASS, Type.ICE, Type.BUG, Type.STEEL);
        bulkMatchup(Type.WATER, Matchup.HALF, Type.WATER, Type.GRASS, Type.DRAGON);
        bulkMatchup(Type.WATER, Matchup.SUPER, Type.FIGHTING, Type.GROUND, Type.ROCK);
        bulkMatchup(Type.ELECTRIC, Matchup.HALF, Type.ELECTRIC, Type.GRASS, Type.DRAGON);
        bulkMatchup(Type.ELECTRIC, Matchup.SUPER, Type.WATER, Type.FLYING);
        matchup(Type.ELECTRIC, Type.GROUND, Matchup.IMMUNE);
        bulkMatchup(Type.GRASS, Matchup.HALF, Type.FIRE, Type.GRASS, Type.POISON, Type.FLYING, Type.BUG, Type.DRAGON, Type.STEEL);
        bulkMatchup(Type.GRASS, Matchup.SUPER, Type.WATER, Type.GROUND, Type.ROCK);
        bulkMatchup(Type.ICE, Matchup.HALF, Type.FIRE, Type.WATER, Type.ICE, Type.STEEL);
        bulkMatchup(Type.ICE, Matchup.SUPER, Type.GRASS, Type.GROUND, Type.FLYING, Type.DRAGON);
        bulkMatchup(Type.FIGHTING, Matchup.HALF, Type.POISON, Type.FLYING, Type.PSYCHIC, Type.BUG, Type.FAIRY);
        bulkMatchup(Type.FIGHTING, Matchup.SUPER, Type.NORMAL, Type.ICE, Type.ROCK, Type.DARK, Type.STEEL);
        matchup(Type.FIGHTING, Type.GHOST, Matchup.IMMUNE);
        bulkMatchup(Type.POISON, Matchup.HALF, Type.POISON, Type.GROUND, Type.ROCK, Type.GHOST);
        bulkMatchup(Type.POISON, Matchup.SUPER, Type.GRASS, Type.FAIRY);
        matchup(Type.POISON, Type.STEEL, Matchup.IMMUNE);
        bulkMatchup(Type.GROUND, Matchup.HALF, Type.GRASS, Type.BUG);
        bulkMatchup(Type.GROUND, Matchup.SUPER, Type.FIRE, Type.ELECTRIC, Type.POISON, Type.ROCK, Type.STEEL);
        matchup(Type.GROUND, Type.FLYING, Matchup.IMMUNE);
        bulkMatchup(Type.FLYING, Matchup.HALF, Type.ELECTRIC, Type.ROCK, Type.STEEL);
        bulkMatchup(Type.FLYING, Matchup.SUPER, Type.GRASS, Type.FIGHTING, Type.BUG);
        bulkMatchup(Type.PSYCHIC, Matchup.HALF, Type.PSYCHIC, Type.STEEL);
        bulkMatchup(Type.PSYCHIC, Matchup.SUPER, Type.FIGHTING, Type.POISON);
        matchup(Type.PSYCHIC, Type.DARK, Matchup.IMMUNE);
        bulkMatchup(Type.BUG, Matchup.HALF, Type.FIRE, Type.FIGHTING, Type.POISON, Type.FLYING, Type.GHOST, Type.STEEL, Type.FAIRY);
        bulkMatchup(Type.BUG, Matchup.SUPER, Type.GRASS, Type.PSYCHIC, Type.DARK);
        bulkMatchup(Type.ROCK, Matchup.HALF, Type.FIGHTING, Type.GROUND, Type.STEEL);
        bulkMatchup(Type.ROCK, Matchup.SUPER, Type.FIRE, Type.WATER, Type.FLYING, Type.BUG);
        matchup(Type.GHOST, Type.PSYCHIC, Matchup.SUPER);
        matchup(Type.GHOST, Type.GHOST, Matchup.SUPER);
        matchup(Type.GHOST, Type.DARK, Matchup.HALF);
        matchup(Type.GHOST, Type.NORMAL, Matchup.IMMUNE);
        matchup(Type.DRAGON, Type.DRAGON, Matchup.SUPER);
        matchup(Type.DRAGON, Type.STEEL, Matchup.HALF);
        matchup(Type.DRAGON, Type.FAIRY, Matchup.IMMUNE);
        bulkMatchup(Type.DARK, Matchup.HALF, Type.FIGHTING, Type.DARK, Type.FAIRY);
        bulkMatchup(Type.DARK, Matchup.SUPER, Type.PSYCHIC, Type.GHOST);
        bulkMatchup(Type.STEEL, Matchup.HALF, Type.FIRE, Type.WATER, Type.ELECTRIC, Type.STEEL);
        bulkMatchup(Type.STEEL, Matchup.SUPER, Type.ICE, Type.ROCK, Type.FAIRY);
        bulkMatchup(Type.FAIRY, Matchup.HALF, Type.FIRE, Type.POISON, Type.STEEL);
        bulkMatchup(Type.FAIRY, Matchup.SUPER, Type.FIGHTING, Type.DRAGON, Type.DARK);
    }

    private static void matchup(Type attack, Type defend, Matchup effectiveness) {
        matchups.computeIfAbsent(attack, t -> new EnumMap<>(Type.class))
                .put(defend, effectiveness);
    }

    private static void bulkMatchup(Type attack, Matchup effectiveness, Type... defends) {
        Arrays.stream(defends)
                .forEach(defend -> matchup(attack, defend, effectiveness));
    }

    public static Matchup getMatchup(Type attack, Type defend) {
        if (matchups.containsKey(attack)) {
            return matchups.get(attack).getOrDefault(defend, Matchup.NORMAL);
        } else {
            return Matchup.NORMAL;
        }
    }

    public enum Matchup {
        IMMUNE,
        HALF,
        NORMAL,
        SUPER
    }
}
