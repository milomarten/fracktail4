package com.github.milomarten.fracktail4.amtrak.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.TimeZone;

@Data
public class Station {
    private String code;
    private String name;
    @JsonProperty("tz") private TimeZone timezone;
    @JsonProperty("lat") private double latitude;
    @JsonProperty("lon") private double longitude;
    private boolean hasAddress;
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private List<TrainId> trains;

    public String getAddressLine() {
        if (address2.isBlank()) {
            return String.join(", ", List.of(address1, city, state, zip));
        } else {
            return String.join(", ", List.of(address1, address2, city, state, zip));
        }
    }
}
