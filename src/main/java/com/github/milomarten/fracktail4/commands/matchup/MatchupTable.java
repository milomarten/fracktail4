package com.github.milomarten.fracktail4.commands.matchup;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.OptionalInt;

public class MatchupTable<T extends Enum<T>> {
    private Class<T> typeClass;
    private Map<T, Map<T, Matchup>> matchups;

    public MatchupTable(Class<T> typeClass) {
        this.typeClass = typeClass;
        this.matchups = new EnumMap<>(typeClass);
    }

    public MatchupTable<T> add(T attacker, T defender, Matchup matchup) {
        matchups.computeIfAbsent(attacker, (k) -> new EnumMap<>(typeClass))
                .put(defender, matchup);
        return this;
    }

    @SafeVarargs
    public final MatchupTable<T> addBulk(T defender, Matchup matchup, T... attackers) {
        Arrays.stream(attackers)
                .forEach(attacker -> add(attacker, defender, matchup));
        return this;
    }

    public Matchup get(T attacker, T defender) {
        if (matchups.containsKey(attacker)) {
            return matchups.get(attacker).getOrDefault(defender, Matchup.NEUTRAL);
        } else {
            return Matchup.NEUTRAL;
        }
    }

    public OptionalInt getMatchup(T attacker, T defender1, T defender2) {
        return Matchup.getMultiMatchupValue(
                get(attacker, defender1),
                (defender2 == null || defender1 == defender2) ? null : get(attacker, defender2)
        );
    }
}
