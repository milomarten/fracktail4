package com.github.milomarten.fracktail4.commands.matchup;

import java.util.OptionalInt;

public enum Matchup {
    NEUTRAL,
    SUPER_EFFECTIVE,
    INEFFECTIVE,
    IMMUNE;

    public static OptionalInt getMultiMatchupValue(Matchup... matchups) {
        int total = 0;
        for (var matchup : matchups) {
            if (matchup == IMMUNE) {
                return OptionalInt.empty();
            } else if (matchup != null) {
                total += switch (matchup) {
                    case NEUTRAL -> 0;
                    case SUPER_EFFECTIVE -> +1;
                    case INEFFECTIVE -> -1;
                    case IMMUNE -> throw new IllegalStateException();
                };
            }
        }
        return OptionalInt.of(total);
    }
}
