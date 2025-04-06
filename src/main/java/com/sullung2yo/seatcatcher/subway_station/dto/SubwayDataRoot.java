package com.sullung2yo.seatcatcher.subway_station.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SubwayDataRoot {
    // Description
    @JsonProperty("DESCRIPTION")
    private Map<String, String> description;

    // Data
    @JsonProperty("DATA")
    private List<SubwayStationData> data;
}
