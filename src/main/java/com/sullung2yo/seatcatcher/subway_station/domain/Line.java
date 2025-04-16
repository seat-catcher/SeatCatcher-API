package com.sullung2yo.seatcatcher.subway_station.domain;

import com.sullung2yo.seatcatcher.common.exception.ErrorCode;
import com.sullung2yo.seatcatcher.common.exception.SubwayException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Line {

    LINE_1("1"),
    LINE_2("2"),
    LINE_3("3"),
    LINE_4("4"),
    LINE_5("5"),
    LINE_6("6"),
    LINE_7("7"),
    LINE_8("8"),
    LINE_9("9"),

    SUIN_BUNDANG("75"),
    GYEONGUI("63"),
    GYEONGCHUN("67"),
    JUNGANG("61"),
    AIRPORT("65"),
    SHINBUNDANG("77"),
    UI_SINSEOL("92"),
    SEOHAE("93");

    private final String name;

    public static Line findByName(String name) {
        try
        {
            return Line.valueOf(name);
        }
        catch (IllegalArgumentException e)
        {
            // 무시하고 다음 로직 수행.
        }

        for (Line line : Line.values()) {
            if (line.getName().equals(name)) {
                return line;
            }
        }
        throw new SubwayException("지하철 노선 정보를 찾을 수 없습니다. : " + name, ErrorCode.SUBWAY_LINE_NOT_FOUND);
    }

    public static String convertForIncomingTrains(String name) {
        try {
            return String.valueOf(Long.parseLong(name) + 1000);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("노선 번호를 4자리 정수로 변환 중 오류가 발생했습니다. : " + name, e);
        }
    }
}
