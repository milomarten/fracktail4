package com.github.milomarten.fracktail.core.amtrak.models;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;

public record TrainId(String trainNumber, int dom) {
    public static TrainId of(String trainNumber, Temporal time) {
        return new TrainId(trainNumber, time.get(ChronoField.DAY_OF_MONTH));
    }

    @JsonCreator
    public static TrainId of(String raw) {
        var tokens = raw.split("-");
        if (tokens.length != 2) {
            throw new IllegalArgumentException(raw);
        }
        return new TrainId(tokens[0], Integer.parseInt(tokens[1]));
    }

    @Override
    public String toString() {
        return trainNumber + "-" + dom;
    }
}
