package com.github.milomarten.fracktail4.amtrak.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

@Data
public class Train {
    private String routeName;
    private String trainNum;
    @JsonProperty("trainID") private String trainId;
    @JsonProperty("lat") private double latitude;
    @JsonProperty("lon") private double longitude;
    @Deprecated private String trainTimely;
    private List<RouteStation> stations;
    private String heading;
    private String eventCode;
    @JsonProperty("eventTZ") private TimeZone eventTimezone;
    private String eventName;
    @JsonProperty("origCode") private String originCode;
    @JsonProperty("origTZ") private TimeZone originTimezone;
    @JsonProperty("origName") private String originName;
    @JsonProperty("destCode") private String destinationCode;
    @JsonProperty("destTZ") private TimeZone destinationTimezone;
    @JsonProperty("destName") private String destinationName;
    private TrainState trainState;
    private double velocity;
    @JsonProperty("statusMsg") private String statusMessage;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    @JsonProperty("lastValTS") private ZonedDateTime lastValueTimestamp;
    @JsonProperty("objectID") private int objectId;
    private TrainProvider provider;
}
