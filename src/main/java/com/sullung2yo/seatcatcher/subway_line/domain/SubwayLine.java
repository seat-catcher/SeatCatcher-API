package com.sullung2yo.seatcatcher.subway_line.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.subway_station.domain.SubwayStation;
import com.sullung2yo.seatcatcher.subway_stations_subway_lines.domain.SubwayStationSubwayLine;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="subway_lines", uniqueConstraints = {@UniqueConstraint(columnNames = {""})})
public class SubwayLine extends BaseEntity {

    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.
    */

    //양방향 Many To Many 구현을 위해 Mapping Table SubwayStationSubwayLine을 이용
    @OneToMany(mappedBy = "subwayLine")
    private Set<SubwayStationSubwayLine> subwayStationSubwayLines;

    @Column(name = "line_name", nullable = false)
    private String lineName;
}
