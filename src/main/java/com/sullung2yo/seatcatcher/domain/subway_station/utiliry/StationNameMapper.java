package com.sullung2yo.seatcatcher.domain.subway_station.utiliry;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class StationNameMapper {
    private static final Map<String, String> stationNameMap = Map.ofEntries(
        Map.entry("숭실대입구", "숭실대입구(살피재)"),
        Map.entry("이수", "총신대입구(이수)"),
        Map.entry("상도", "상도(중앙대앞)"),
        Map.entry("군자", "군자(능동)"),
        Map.entry("공릉", "공릉(서울산업대입구)"),
        Map.entry("어린이대공원", "어린이대공원(세종대)")
    );

    public String mapToApiName(String name)
    {
        return stationNameMap.getOrDefault(name, name); // 이름이 검색되지 않으면 그 이름 그대로 리턴.
    }
}
