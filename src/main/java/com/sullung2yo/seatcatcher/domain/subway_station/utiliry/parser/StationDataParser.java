package com.sullung2yo.seatcatcher.domain.subway_station.utiliry.parser;

import com.sullung2yo.seatcatcher.domain.subway_station.dto.SubwayStationData;

import java.io.IOException;
import java.util.List;

public interface StationDataParser {

    List<SubwayStationData> parseJsonData(String filePath) throws IOException;
}
