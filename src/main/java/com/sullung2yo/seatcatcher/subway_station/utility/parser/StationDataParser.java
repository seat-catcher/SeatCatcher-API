package com.sullung2yo.seatcatcher.subway_station.utility.parser;

import com.sullung2yo.seatcatcher.subway_station.dto.SubwayStationData;

import java.io.IOException;
import java.util.List;

public interface StationDataParser {

    List<SubwayStationData> parseJsonData() throws IOException;
}
