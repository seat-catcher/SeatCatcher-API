package com.sullung2yo.seatcatcher.subway_station.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="subway_stations_subway_lines")
public class SubwayStationSubwayLine extends BaseEntity {

    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.

        이 Entity 는 Mapping Table입니다! SubwayStation 과 SubwayLine 의
        ManyToMany 관계를 구현하기 위해 생성되었습니다.
    */
    @ManyToOne
    @JoinColumn(name = "station_id")
    private SubwayStation subwayStation;

    @ManyToOne
    @JoinColumn(name = "line_id")
    private SubwayLine subwayLine;

    public void setRelationships(SubwayStation subwayStation, SubwayLine subwayLine) {
        this.subwayLine = subwayLine;
        this.subwayStation = subwayStation;

        subwayStation.getSubwayStationSubwayLines().add(this);
        subwayLine.getSubwayStationSubwayLines().add(this);
    }
}
