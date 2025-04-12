package com.sullung2yo.seatcatcher.subway_station.domain;

import jakarta.persistence.*;
import lombok.*;
import com.sullung2yo.seatcatcher.common.domain.BaseEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 일단 임의로 테이블 이름을 subway_station 이라고 해두겠습니다.
@Table(name="subway_stations")
public class SubwayStation extends BaseEntity{

    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.
    */

    @Column(name="station_name", nullable = false)
    private String stationName; // 역의 이름입니다. (subway stations name)

    @Enumerated(EnumType.STRING)
    @Column(name="line", nullable = false)
    private Line line; // 호선 정보입니다.

    @Column(name="dist_km", nullable = false)
    private float distance; // 전 역에서 해당 역까지의 거리입니다.

    @Column(name="time_min_sec", nullable = false)
    private String timeMinSec; // 전 역에서 해당 역까지 오는 데에 걸리는 시간입니다. "분:초" 형식으로 제공됩니다. (hour-minute)

    @Column(name="acml_dist", nullable = false)
    private float accumulateDistance; // 첫 역부터 해당 역까지 도착했을 때 기준 누계 거리입니다.

    @Column(name="acml_time", nullable = false)
    private long accumulateTime; // 첫 역부터 해당 역까지 도착했을 때 기준 누계 시간입니다. (seconds)
    /*
        TODO :: 해당 칼럼은 데이터 소스에서 제공되지 않습니다! 데이터 삽입을 할 때 주어진 데이터로 계산해야 합니다.

        계산 방식은 다음과 같습니다.

        현재 역의 acml_hm = 해당 역보다 acml_dist 가 작은 같은 호선에 있는
        열차들의 hm 값들 + 자신의 hm 값을 모두 더한 값.

        예를 들어 1호선 종각역의 경우 종각역의 hm(2:00) + 시청역의 hm(2:00) + 서울역의 hm(0:00) = 4:00

        해당 값은 "얼마나 지하철에서 서서 가야 하는가" 를 나타내주는 데이터로 쓰입니다!
    */

    // 소스에서 데이터가 "분:초" 형식으로 오는데, 이걸 넣으면 그게 몇 초인지 리턴해주는 함수입니다!
    public long convertStringToSeconds(String timeString) {

        if(timeString == null || timeString.isEmpty() || !timeString.contains(":")) {
            throw new IllegalArgumentException("Invalid time string: " + timeString);
        }

        String[] timeParts = timeString.split(":");
        try
        {
            int minutes = Integer.parseInt(timeParts[0]);
            int seconds = Integer.parseInt(timeParts[1]);

            if(seconds < 0 || seconds > 59) {
                throw new IllegalArgumentException("Invalid time string: " + timeString);
            }

            return (long)minutes * 60 + seconds;
        }
        catch(NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid time string: " + timeString + ": " + e.getMessage());
        }

    }
}
