package com.sullung2yo.seatcatcher.subway_station.domain;

import com.sullung2yo.seatcatcher.config.exception.ErrorCode;
import com.sullung2yo.seatcatcher.config.exception.SubwayLineNotFoundException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

    BUNDANG("분당선"),
    SUIN("수인선"),
    SUIN_BUNDANG("수인·분당선"),
    GYEONGUI("경의선"),
    GYEONGCHUN("경춘선"),
    GYEONGWON("경원선"),
    GYEONGBU("경부선"),
    JUNGANG("중앙선"),
    AIRPORT("공항철도"),
    SHINBUNDANG("신분당선"),
    UI_LINE("의정부경전철"),
    EVERLINE("용인경전철"),
    UI_SINSEOL("우이신설선"),
    SEOHAE("서해선"),
    INCHEON_LINE_1("인천1호선"),
    INCHEON_LINE_2("인천2호선"),
    GIMPO_GOLDLINE("김포골드라인"),
    SILLIM("신림선");

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
        throw new SubwayLineNotFoundException("지하철 노선 정보를 찾을 수 없습니다. : " + name, ErrorCode.SUBWAY_LINE_NOT_FOUND);
    }
}
