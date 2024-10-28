package com.github.milomarten.fracktail4.amtrak.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StaleStatus {
    @JsonProperty("avgLastUpdate") private double averageLastUpdate;
    private int activeTrains;
    private boolean stale;
}
