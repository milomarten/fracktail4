package com.github.milomarten.fracktail4.birthday;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.time.Month;
import java.util.Map;
import java.util.Optional;

public class BirthdayUtils {
    private static final Map<String, Month> options = new CaseInsensitiveMap<>(12*3);
    static {
        for (var month : Month.values()) {
            options.put(month.name(), month);
            options.put(String.valueOf(month.getValue()), month);
            options.put(month.name().substring(0, 3), month);
        }
    }

    public static Optional<Month> parseMonth(String raw) {
        return Optional.ofNullable(options.get(raw));
    }

    public static String getDisplayMonth(Month month) {
        var raw = month.name();
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1).toLowerCase();
    }
}
