package com.github.milomarten.fracktail.core.amtrak.models;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonProperty;

public enum RouteStatus {
    @JsonProperty("Enroute") ENROUTE,
    @JsonProperty("Station") STATION,
    @JsonProperty("Departed") DEPARTED,
    @JsonEnumDefaultValue UNKNOWN
}
