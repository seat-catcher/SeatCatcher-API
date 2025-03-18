package com.sullung2yo.seatcatcher.subway_station.domain;

import jakarta.persistence.*;
import lombok.*;
import com.sullung2yo.seatcatcher.common.domain.BaseEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 일단 임의로 테이블 이름을 subway_station 이라고 해두겠습니다.
@Table(name="subway_station", uniqueConstraints = {@UniqueConstraint(columnNames = {"sbwy_stns_nm", "sbwy_rout_ln"})})
public class SubwayStation extends BaseEntity{

    /*
        BaseEntity 를 상속받아
        id
        created_at
        modified_at
        해당 세 칼럼은 정의하지 않았습니다.
    */
    @Column(name="sbwy_stns_nm", nullable = false)
    private String name; // 역의 이름입니다. (subway stations name)

    //TODO :: 데이터셋에는 string으로 되어 있습니다만, Jackson이 알아서 변환도 해준다고 해서 int로 뒀습니다. 확인이 필요합니다!
    @Column(name="sbwy_rout_ln", nullable = false)
    private int routeLine; // 호선 번호입니다. (subway route line)

    @Column(name="dist_km", nullable = false)
    private float distance; // 전 역에서 해당 역까지의 거리입니다.

    @Column(name="hm", nullable = false)
    private String hourMinute; // 전 역에서 해당 역까지 오는 데에 걸리는 시간입니다. "분:초" 형식으로 제공됩니다. (hour-minute)

    @Column(name="acml_dist", nullable = false)
    private float accumulateDistance; // 첫 역부터 해당 역까지 도착했을 때 기준 누계 거리입니다.

    @Column(name="acml_hm", nullable = false)
    private String accumulateHourMinute; // 첫 역부터 해당 역까지 도착했을 때 기준 누계 시간입니다.
    /*
        TODO :: 해당 칼럼은 데이터 소스에서 제공되지 않습니다! 데이터 삽입을 할 때 주어진 데이터로 계산해야 합니다.

        계산 방식은 다음과 같습니다.

        현재 역의 acml_hm = 해당 역보다 acml_dist 가 작은 같은 호선에 있는
        열차들의 hm 값들 + 자신의 hm 값을 모두 더한 값.

        예를 들어 1호선 종각역의 경우 종각역의 hm(2:00) + 시청역의 hm(2:00) + 서울역의 hm(0:00) = 4:00

        해당 값은 "얼마나 지하철에서 서서 가야 하는가" 를 나타내주는 데이터로 쓰입니다!
    */

    // 전 역에서 해당 역까지 오는 데에 걸리는 시간을 초로 변환해주는 함수입니다!
    public long getHourMinuteInSeconds() {
        return convertStringToSeconds(hourMinute);
    }

    // 누계 시간을 초로 변환해주는 함수입니다!
    public long getAccumulateHourMinuteInSeconds() {
        return convertStringToSeconds(accumulateHourMinute);
    }

    // 소스에서 데이터가 "분:초" 형식으로 오는데, 이걸 넣으면 그게 몇 초인지 리턴해주는 함수입니다!
    private long convertStringToSeconds(String timeString) {

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
