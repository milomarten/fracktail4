package com.github.milomarten.fracktail4.commands.amtrak.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum TrainProvider {
    @JsonProperty("Amtrak") AMTRAK,
    @JsonProperty("Via") VIA,
    @JsonProperty("Brightline") BRIGHTLINE,
    @JsonEnumDefaultValue UNKNOWN
}
