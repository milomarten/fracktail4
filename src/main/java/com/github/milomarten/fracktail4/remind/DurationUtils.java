package com.github.milomarten.fracktail4.remind;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.IteratorUtils;

import java.time.Duration;
import java.util.*;

public class DurationUtils {
    private static final Set<String> HOURS = Set.of("h", "hr", "hrs", "hours", "hour");
    private static final Set<String> MINUTES = Set.of("m", "min", "mins", "minutes", "minute");
    private static final Set<String> SECONDS = Set.of("s", "sec", "secs", "seconds", "second");

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
        OptionalInt hours = OptionalInt.empty();
        OptionalInt minutes = OptionalInt.empty();
        OptionalInt seconds = OptionalInt.empty();

        Iterator<Character> iter = IteratorUtils.arrayIterator(str.toCharArray());
        ParserState stateMachine = new NumberParser("");
        while (iter.hasNext()) {
            char c = iter.next();
            var result = stateMachine.consumeCharacter(c);
            if (result.isPresent()) {
                var updateMaybe = stateMachine.getPiece();
                stateMachine = result.get();
                if (updateMaybe.isPresent()) {
                    var update = updateMaybe.get();
                    switch (update.unit) {
                        case HOUR -> hours = updateOnce(hours, update.number);
                        case MINUTE -> minutes = updateOnce(minutes, update.number);
                        case SECOND -> seconds = updateOnce(seconds, update.number);
                    }
                }
            }
        }

        var update = stateMachine.getPiece().orElseThrow(() -> new IllegalStateException("Unexpected ending"));
        switch (update.unit) {
            case HOUR -> hours = updateOnce(hours, update.number);
            case MINUTE -> minutes = updateOnce(minutes, update.number);
            case SECOND -> seconds = updateOnce(seconds, update.number);
        }

        return Duration.ofHours(hours.orElse(0))
                .plusMinutes(minutes.orElse(0))
                .plusSeconds(seconds.orElse(0));
    }

    private static OptionalInt updateOnce(OptionalInt container, int value) {
        if (container.isPresent()) {
            throw new IllegalStateException("Unit specified twice.");
        } else {
            return OptionalInt.of(value);
        }
    }

    @RequiredArgsConstructor
    private enum Unit {
        HOUR(HOURS),
        MINUTE(MINUTES),
        SECOND(SECONDS);

        private final Set<String> verbiage;

        public static Optional<Unit> find(String word) {
            var lowercase = word.toLowerCase();
            for (var unit : Unit.values()) {
                if (unit.verbiage.contains(lowercase)) {
                    return Optional.of(unit);
                }
            }
            return Optional.empty();
        }
    }

    private record DurationPiece(int number, Unit unit) {}

    private interface ParserState {
        Optional<ParserState> consumeCharacter(char c);
        Optional<DurationPiece> getPiece();
    }

    @AllArgsConstructor
    private static class NumberParser implements ParserState {
        private String number;

        @Override
        public Optional<ParserState> consumeCharacter(char c) {
            if (Character.isDigit(c)) {
                number += c;
                return Optional.empty();
            } else {
                if (number.isEmpty()) {
                    throw new IllegalStateException("No number present when expected.");
                }
                return Optional.of(new UnitParser(Integer.parseInt(number), String.valueOf(c)));
            }
        }

        @Override
        public Optional<DurationPiece> getPiece() {
            return Optional.empty();
        }
    }

    @AllArgsConstructor
    private static class UnitParser implements ParserState {
        private final int number;
        private String unit;

        @Override
        public Optional<ParserState> consumeCharacter(char c) {
            if (Character.isDigit(c)) {
                return Optional.of(new NumberParser(String.valueOf(c)));
            } else {
                unit += c;
                return Optional.empty();
            }
        }

        @Override
        public Optional<DurationPiece> getPiece() {
            var unit = Unit.find(this.unit)
                    .orElseThrow(() -> new IllegalStateException("Unknown unit " + this.unit));
            return Optional.of(new DurationPiece(number, unit));
        }
    }
}
