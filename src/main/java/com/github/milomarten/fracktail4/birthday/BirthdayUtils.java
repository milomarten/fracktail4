package com.github.milomarten.fracktail4.birthday;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Arrays;
import java.util.List;
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

    private static final String[] ordinals = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
    public static String getDisplayBirthday(MonthDay monthDay) {
        String month = getDisplayMonth(monthDay.getMonth());
        String rawDay = String.valueOf(monthDay.getDayOfMonth());
        String day;
        if (rawDay.length() == 2 && rawDay.charAt(0) == '1') {
            // 10 to 19 all use the same ordinal, th
            day = rawDay + "th";
        } else {
            int digit = (rawDay.length() == 2 ? rawDay.charAt(1) : rawDay.charAt(0)) - '0';
            day = rawDay + ordinals[digit];
        }
        return month + " " + day;
    }

    public static List<ApplicationCommandOptionChoiceData> getMonthOptions() {
        return Arrays.stream(Month.values())
                .map(month -> {
                    return (ApplicationCommandOptionChoiceData) ApplicationCommandOptionChoiceData.builder()
                            .name(getDisplayMonth(month))
                            .value(month.name())
                            .build();
                }).toList();
    }

    public static String getDurationWords(LocalDate origin, LocalDate to) {
        var period = origin.until(to, ChronoUnit.DAYS);
        var plural = (period == 1 || period == -1) ? "" : "s";
        if (period < 0) {
            return String.format("%d day%s ago", -period, plural);
        } else {
            return String.format("%d day%s from now", period, plural);
        }
    }

    public static String getName(Member member) {
        String username = getName((User)member);
        return member.getNickname()
                .map(n -> n + " (AKA " + username + ")")
                .orElse(username);
    }

    public static String getName(User user) {
        return user.getGlobalName()
                .orElseGet(user::getUsername);
    }
}
