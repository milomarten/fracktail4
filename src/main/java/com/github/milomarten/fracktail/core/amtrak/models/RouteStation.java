package com.github.milomarten.fracktail.core.amtrak.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;
import java.util.TimeZone;

@Data
public class RouteStation {
    private String code;
    private String name;
    @JsonProperty("tz") private TimeZone timezone;
    private boolean bus;
    @JsonProperty("schArr") private ZonedDateTime scheduledArrival;
    @JsonProperty("schDep") private ZonedDateTime scheduledDeparture;
    @JsonProperty("arr") private ZonedDateTime arrival;
    @JsonProperty("dep") private ZonedDateTime departure;
    @Deprecated private String arrCmnt;
    @Deprecated private String depCmnt;
    private String platform;
    private RouteStatus status;
}
