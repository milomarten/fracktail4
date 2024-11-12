package com.github.milomarten.fracktail4.remind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DurationUtils {
    private static final Set<String> HOURS = Set.of("h", "hr", "hrs", "hours", "hour");
    private static final Set<String> MINUTES = Set.of("m", "min", "mins", "minutes", "minute");
    private static final Set<String> SECONDS = Set.of("s", "sec", "secs", "seconds", "second");

    private static final Pattern FORMAT_PATTERN =
            Pattern.compile(String.format("(([0-9]+)%s)?(([0-9]+)%s)?(([0-9]+)%s)?", regexForUnit(HOURS), regexForUnit(MINUTES), regexForUnit(SECONDS)));

    public static boolean isValidFormat(String s) {
        return FORMAT_PATTERN.matcher(s).matches();
    }

    private static String regexForUnit(Set<String> s) {
        return s.stream()
                .sorted(Comparator.comparing(str -> -str.length()))
                .collect(Collectors.joining("|", "(", ")"));
    }

    public static String durationToString(Duration duration) {
        var days = duration.toDaysPart();
        var hours = duration.toHoursPart();
        var minutes = duration.toMinutesPart();
        var seconds = duration.toSecondsPart();

        List<String> sj = new ArrayList<>();
        if (days > 0) { sj.add(formatUnit("day", days)); }
        if (hours > 0) { sj.add(formatUnit("hour", hours)); }
        if (minutes > 0) { sj.add(formatUnit("minute", minutes)); }
        if (seconds > 0) { sj.add(formatUnit("second", seconds)); }

        if (sj.isEmpty()) {
            return "Now";
        } else if (sj.size() == 1) {
            return sj.get(0);
        } else if (sj.size() == 2) {
            return sj.get(0) + " and " + sj.get(1);
        } else {
            var lastIdx = sj.size() - 1;
            var last = sj.get(lastIdx);
            sj.set(lastIdx, "and " + last);
            return String.join(", ", sj);
        }
    }

    private static String formatUnit(String unit, long number) {
        return number + " " + unit + (number == 1 ? "" : "s");
    }

    public static Duration stringToDuration(String str) {
        var matcher = FORMAT_PATTERN.matcher(str);
        if (matcher.matches()) {
            var hoursRaw = StringUtils.defaultIfBlank(matcher.group(2), "0");
            var minutesRaw = StringUtils.defaultIfBlank(matcher.group(5), "0");
            var secondsRaw = StringUtils.defaultIfBlank(matcher.group(8), "0");

            return Duration.ofHours(Integer.parseInt(hoursRaw))
                    .plusMinutes(Integer.parseInt(minutesRaw))
                    .plusSeconds(Integer.parseInt(secondsRaw));
        } else {
            throw new IllegalArgumentException("Incorrect format");
        }
    }
}
