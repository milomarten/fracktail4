package com.github.milomarten.fracktail4.amtrak.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum TrainState {
    @JsonProperty("Predeparture") PREDEPARTURE,
    @JsonProperty("Active") ACTIVE,
    @JsonProperty("Completed") COMPLETED,
    @JsonEnumDefaultValue UNKNOWN
}
