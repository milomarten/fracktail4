package com.github.milomarten.fracktail4.commands.amtrak.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RouteStatus {
    @JsonProperty("Enroute") ENROUTE,
    @JsonProperty("Station") STATION,
    @JsonProperty("Departed") DEPARTED,
    @JsonEnumDefaultValue UNKNOWN
}
