package com.sullung2yo.seatcatcher.subway_station.domain;

import com.sullung2yo.seatcatcher.common.domain.BaseEntity;
import com.sullung2yo.seatcatcher.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="path_bookmarks")
public class PathBookmark extends BaseEntity {
    /*
        BaseEntity 를 상속받아
        id   ::   id 는 지하철 역에 대한 고유 식별자로써, 인덱싱 역할을 수행합니다.
        created_at    ::    데이터베이스에 저장된 날짜입니다.
        modified_at    ::    데이터베이스에서 수정된 날짜입니다.
        해당 세 칼럼은 정의하지 않았습니다.
    */

    //@ManyToOne
    @ManyToOne
    @JoinColumn(name ="id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private SubwayStation startStation;

    @ManyToOne
    @JoinColumn(name = "id", nullable = false)
    private SubwayStation endStation;

    @Column(name="use_count")
    private int useCount; // 경로가 사용된 횟수
}
