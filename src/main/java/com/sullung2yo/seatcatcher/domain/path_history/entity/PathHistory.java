package com.sullung2yo.seatcatcher.domain.path_history.entity;

import com.sullung2yo.seatcatcher.common.domain.base.entity.BaseEntity;
import com.sullung2yo.seatcatcher.domain.subway_station.entity.SubwayStation;
import com.sullung2yo.seatcatcher.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="path_histories")
public class PathHistory extends BaseEntity {
    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.
    */

    //@ManyToOne
    @ManyToOne
    @JoinColumn(name ="user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "start_station_id", nullable = false)
    private SubwayStation startStation;

    @ManyToOne
    @JoinColumn(name = "end_station_id", nullable = false)
    private SubwayStation endStation;

    /*
        PathHistory 가 실제로 StartStation, EndStation 을 기반으로 만들어질 때
        calculateExpectedArrivalTime 을 호출하여 예상 도착 시간을 계산해주셔야 합니다!
    */
    @Column(name="expected_arrival_time")
    private LocalDateTime expectedArrivalTime;

    public void calculateExpectedArrivalTime(SubwayStation startStation, SubwayStation endStation) {
        this.expectedArrivalTime = getCreatedAt().plusSeconds(
                Math.abs(
                        endStation.getAccumulateTime() - startStation.getAccumulateTime() // 데이터소스에서 넣을 때 계산해서 넣기때문에 getAccumulateTime()만 호출하도록 변경
                )
        );
    }
}

